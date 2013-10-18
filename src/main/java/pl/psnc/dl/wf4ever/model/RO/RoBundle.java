package pl.psnc.dl.wf4ever.model.RO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.db.ResourceInfo;
import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.ORE.Proxy;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.sparql.RO_RDFXMLWriter;
import pl.psnc.dl.wf4ever.sparql.RO_TurtleWriter;
import pl.psnc.dl.wf4ever.sparql.ResearchObjectRelativeWriter;
import pl.psnc.dl.wf4ever.util.MimeTypeUtil;
import pl.psnc.dl.wf4ever.vocabulary.ORE;

import com.github.jsonldjava.core.JSONLD;
import com.github.jsonldjava.core.JSONLDProcessingError;
import com.github.jsonldjava.core.JSONLDTripleCallback;
import com.github.jsonldjava.core.Options;
import com.github.jsonldjava.impl.JenaTripleCallback;
import com.github.jsonldjava.utils.JSONUtils;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;

/**
 * A special resource that is an RO Bundle ({@link http://wf4ever.github.io/ro/bundle/}). When saved, it creates a new
 * research object that is dependent on this bundle.
 * 
 * @author piotrekhol
 * 
 */
public class RoBundle extends Resource {

    /**
     * A resource that takes its content from the ZIP bundle.
     * 
     * @author piotrekhol
     * 
     */
    private class BundleResource extends Resource {

        /** The ZIP file that may contain the content of this resource. */
        private ZipFile zipFile;


        /**
         * Constructor.
         * 
         * @param builder
         *            model builder that will be used
         * @param uri
         *            resource URI
         * @param bundleResearchObject
         *            abstract RO aggregating this resource
         * @param creator
         *            resource creator
         * @param created
         *            resource created date
         * @param zipFile
         *            ZIP archive that may have the contents of this resource
         */
        public BundleResource(Builder builder, URI uri, ResearchObject bundleResearchObject, UserMetadata creator,
                DateTime created, ZipFile zipFile) {
            super(builder.getUser(), builder.getDataset(), builder.isUseTransactions(), bundleResearchObject, uri);
            setCreator(creator);
            setCreated(created);
            setBuilder(builder);
            this.zipFile = zipFile;
        }


        @Override
        public boolean isInternal() {
            return zipFile.getEntry(getPath()) != null;
        }


        @Override
        public InputStream getSerialization() {
            ZipEntry entry = zipFile.getEntry(getPath());
            if (entry == null) {
                return null;
            }
            try {
                return zipFile.getInputStream(entry);
            } catch (IOException e) {
                LOGGER.error("Can't read the content from the ZIP archive for " + getUri(), e);
                return null;
            }
        }


        @Override
        public ResourceMetadata getStats() {
            return new ResourceInfo(getPath(), getName(), null, 0, null, null, MimeTypeUtil.getContentType(getPath()));
        }

    }


    /**
     * A resource taken from the RO bundle, which is parsed as an RDF graph before returning the input stream.
     * 
     * @author piotrekhol
     * 
     */
    private class BundleRDFResource extends BundleResource {

        /**
         * Constructor.
         * 
         * @param builder
         *            model builder that will be used
         * @param uri
         *            resource URI
         * @param bundleResearchObject
         *            abstract RO aggregating this resource
         * @param creator
         *            resource creator
         * @param created
         *            resource created date
         * @param zipFile
         *            ZIP archive that may have the contents of this resource
         */
        public BundleRDFResource(Builder builder, URI uri, ResearchObject bundleResearchObject, UserMetadata creator,
                DateTime created, ZipFile zipFile) {
            super(builder, uri, bundleResearchObject, creator, created, zipFile);
        }


        @Override
        public InputStream getSerialization() {
            Model model = ModelFactory.createDefaultModel();
            RDFFormat syntax = RDFFormat.forFileName(getName(), RDFFormat.RDFXML);
            model.read(super.getSerialization(), researchObject.getUri().toString(), syntax.getName().toUpperCase());
            ResearchObjectRelativeWriter writer;
            if (syntax != RDFFormat.RDFXML && syntax != RDFFormat.TURTLE) {
                throw new IllegalArgumentException("Format " + syntax + " is not supported");
            } else if (syntax == RDFFormat.RDFXML) {
                writer = new RO_RDFXMLWriter();
            } else {
                writer = new RO_TurtleWriter();
            }
            writer.setResearchObjectURI(researchObject.getUri());
            writer.setBaseURI(researchObject.getUri().resolve(uri));
            // URI validation in Jena 2.10.0 doesn't allow relative URIs
            writer.setProperty("allowBadURIs", true);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writer.write(model, out, "");
            return new ByteArrayInputStream(out.toByteArray());
        }

    }


    /**
     * A subclass of folder that simplifies adding folder entries without saving any metadata.
     * 
     * @author piotrekhol
     * 
     */
    private class BundleFolder extends Folder {

        /** Folder entries. */
        private Map<URI, FolderEntry> folderEntries = new HashMap<>();


        /**
         * Constructor.
         * 
         * @param builder
         *            model builder that will be used
         * @param uri
         *            resource URI
         * @param researchObject
         *            abstract RO aggregating this resource
         * @param creator
         *            resource creator
         * @param created
         *            resource created date
         */
        public BundleFolder(Builder builder, ResearchObject researchObject, URI uri, UserMetadata creator,
                DateTime created) {
            super(builder.getUser(), builder.getDataset(), builder.isUseTransactions(), researchObject, uri, null);
            setCreator(creator);
            setCreated(created);
            setBuilder(builder);
        }


        /**
         * Create a new folder entry for a resource.
         * 
         * @param resource
         *            the resource
         */
        public void addFolderEntry(AggregatedResource resource) {
            URI entryUri = uri.resolve(UUID.randomUUID().toString());
            FolderEntry entry = new FolderEntry(user, dataset, useTransactions, entryUri);
            entry.setProxyFor(resource);
            entry.setProxyIn(this);
            folderEntries.put(entryUri, entry);
        }


        @Override
        public Map<URI, FolderEntry> getFolderEntries() {
            return folderEntries;
        }

    }


    /** RO bundle MIME type. */
    public static final String MIME_TYPE = "application/vnd.wf4ever.robundle+zip";

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(RoBundle.class);

    /** the default location of the manifest. */
    private static final String DEFAULT_MANIFEST_PATH = ".ro/manifest.json";


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     * @param dataset
     *            custom dataset
     * @param useTransactions
     *            should transactions be used. Note that not using transactions on a dataset which already uses
     *            transactions may make it unreadable.
     * @param researchObject
     *            The RO it is aggregated by
     * @param uri
     *            resource URI
     */
    public RoBundle(UserMetadata user, Dataset dataset, boolean useTransactions, ResearchObject researchObject, URI uri) {
        super(user, dataset, useTransactions, researchObject, uri);
        LOGGER.debug("Loaded an RO bundle");
    }


    @Override
    public void save(InputStream content, String contentType)
            throws BadRequestException {
        super.save(content, contentType);
        // let's unpack it and see what's inside
        ResearchObject nestedResearchObject = createdNestedRO();
        createLinks(nestedResearchObject);
        copyAnnotations(nestedResearchObject, researchObject);
        this.uri = nestedResearchObject.getUri();
        // save the MIME type of nested RO to the MIME type of the bundle
        // it's necessary for the parent RO to know that it's a bundle
        // the path is "" because we talk about the RO itself
        builder.getDigitalLibrary().updateFileInfo(uri, "", contentType);
    }


    /**
     * Copy all annotation stubs from the first research object to the second. The target of the annotations will be the
     * entire nested RO. The bodies will remain the same, i.e. resources aggregated by the nested RO.
     * 
     * @param nestedResearchObject
     *            the RO to copy from
     * @param researchObject
     *            the RO to copy to
     */
    private void copyAnnotations(ResearchObject nestedResearchObject, ResearchObject researchObject) {
        for (Annotation annotation : nestedResearchObject.getAnnotations().values()) {
            try {
                researchObject.annotate(annotation.getBody().getUri(), nestedResearchObject);
            } catch (BadRequestException e) {
                LOGGER.error("Error when copying annotation stubs from " + nestedResearchObject.getUri() + " to "
                        + researchObject.getUri(), e);
            }
        }
    }


    /**
     * Save links in manifests of this RO and the nested RO.
     * 
     * @param nestedResearchObject
     *            nested RO
     */
    private void createLinks(ResearchObject nestedResearchObject) {
        AggregatedResource nestedAggregatedResource = builder.buildAggregatedResource(nestedResearchObject.getUri(),
            researchObject, creator, created);
        researchObject.getManifest().deleteResource(this.getProxy());
        getProxy().setProxyFor(nestedAggregatedResource);
        researchObject.getManifest().saveProxy(getProxy());

        researchObject.getManifest().deleteResource(this);
        researchObject.getManifest().saveNestedResearchObject(nestedResearchObject);
        researchObject.getManifest().saveAuthor(nestedResearchObject);
        nestedResearchObject.getManifest().saveIsNestedInResearchObject(researchObject);
    }


    /**
     * Unpack the RO bundle and create/update the nested RO.
     * 
     * @return the nested research object
     * 
     * @throws BadRequestException
     *             when this resource is not a valid RO bundle
     */
    private ResearchObject createdNestedRO()
            throws BadRequestException {
        try {
            File temp = File.createTempFile("bundle" + getName(), ".zip");
            FileOutputStream outputStream = new FileOutputStream(temp);
            IOUtils.copy(getSerialization(), outputStream);
            try (ZipFile zipFile = new ZipFile(temp)) {
                URI base = URI.create("app://" + UUID.randomUUID() + "/");
                com.hp.hpl.jena.rdf.model.Resource manifestR = loadManifest(zipFile, base);
                //                manifestR.getModel().write(System.out, "TURTLE");
                ResIterator it = manifestR.getModel().listSubjectsWithProperty(ORE.isDescribedBy, manifestR);
                com.hp.hpl.jena.rdf.model.Resource bundleR;
                if (it.hasNext()) {
                    bundleR = it.next();
                } else {
                    throw new BadRequestException("Can't find the RO bundle described by " + manifestR.getURI()
                            + " in the RO bundle.");
                }
                // An abstract RO representing the RO in the bundle. Useful for resolving resource paths, for example.
                ResearchObject bundleResearchObject = builder.buildResearchObject(URI.create(bundleR.getURI()));

                // The ZIP archive is passed to extract resource contents
                Collection<? extends Resource> resources = extractResourcesFromBundle(bundleR, bundleResearchObject,
                    zipFile);
                LOGGER.info("Identified " + resources.size() + " valid aggregated resources in " + getName());
                // The ZIP archive is passed to extract annotation bodies
                Collection<? extends Annotation> annotations = extractAnnotationsFromBundle(bundleR,
                    bundleResearchObject, zipFile);
                LOGGER.info("Identified " + annotations.size() + " valid annotations in " + getName());
                // The ZIP archive is passed to extract folders
                Collection<? extends Folder> folders = extractFoldersFromBundle(bundleR, bundleResearchObject, zipFile);
                LOGGER.info("Identified " + folders.size() + " valid folders in " + getName());

                // Create the nested RO
                URI nestedROUri = calculateNestedROUri();
                ResearchObject nestedResearchObject = ResearchObject.create(builder, nestedROUri, resources,
                    annotations, folders);
                LOGGER.info("Created a nested RO " + nestedResearchObject.getUri());
                nestedResearchObject.setBundleUri(this.uri);
                return nestedResearchObject;
            }
        } catch (IOException e) {
            throw new BadRequestException("Can't parse the RO bundle: " + e.getMessage(), e);
        }
    }


    /**
     * Find the manifest, convert it to RDF and create the manifest resource.
     * 
     * @param zipFile
     *            the ZIP archive
     * @param base
     *            base URI to use when resolving relative URIs
     * @return the RDF manifest resource
     * @throws IOException
     *             when the ZIP archive is corrupt
     * @throws BadRequestException
     *             when the ZIP archive is not a valid RO bundle
     */
    com.hp.hpl.jena.rdf.model.Resource loadManifest(ZipFile zipFile, URI base)
            throws IOException, BadRequestException {
        // let's assume that the manifest is .ro/manifest.json. In the future we may take this path from the container (META-INF/container.xml)
        // see the RO Bundle spec at http://wf4ever.github.io/ro/bundle/
        String manifestPath = DEFAULT_MANIFEST_PATH;
        ZipEntry manifestEntry = zipFile.getEntry(manifestPath);
        if (manifestEntry == null) {
            throw new BadRequestException("Can't find the manifest " + manifestPath + " in the RO bundle.");
        }
        try (InputStream manifestInputStream = zipFile.getInputStream(manifestEntry)) {
            Object input = JSONUtils.fromInputStream(manifestInputStream);
            JSONLDTripleCallback callback = new JenaTripleCallback();
            Model model = (Model) JSONLD.toRDF(input, callback, new Options(base.toString()));
            return model.getResource(base.resolve(manifestPath).toString());
        } catch (JSONLDProcessingError e) {
            throw new BadRequestException("Can't parse the manifest " + manifestPath, e);
        }
    }


    /**
     * Calculate the URI of the nested RO. The identifier will be based on the name of the bundle without the extension.
     * 
     * @return the RO URI
     */
    private URI calculateNestedROUri() {
        String nameWithoutExtension = getName();
        if (nameWithoutExtension.contains(".")) {
            nameWithoutExtension = nameWithoutExtension.substring(0, nameWithoutExtension.lastIndexOf('.'));
        }
        URI nestedROUri = this.researchObject.getUri().resolve(
            "../" + this.getResearchObject().getName() + "-" + nameWithoutExtension + "/");
        return nestedROUri;
    }


    /**
     * Extract resources based on the manifest in RDF and the contents of the ZIP bundle. The resources will be special
     * subclasses of resources that return their content from the ZIP archive.
     * 
     * @param bundleR
     *            the bundle RDF resource
     * @param bundleResearchObject
     *            abstract RO that will aggregate all resources
     * @param zipFile
     *            ZIP archive
     * @return a set of resources that are not folders nor annotations
     */
    private Set<? extends Resource> extractResourcesFromBundle(com.hp.hpl.jena.rdf.model.Resource bundleR,
            ResearchObject bundleResearchObject, ZipFile zipFile) {
        Set<Resource> resources = new HashSet<>();
        String queryString = String
                .format(
                    "PREFIX ore: <%s> SELECT ?resource ?proxy ?created WHERE { <%s> ore:aggregates ?resource . ?resource <http://purl.org/pav/createdOn> ?created ; <http://purl.org/wf4ever/bundle#hasProxy> ?proxy . FILTER NOT EXISTS { ?other_resource ore:proxyIn ?resource . } }",
                    ORE.NAMESPACE, bundleR.getURI());
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, bundleR.getModel());
        try {
            ResultSet results = qe.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                RDFNode resourceR = solution.get("?resource");
                RDFNode proxyR = solution.get("?proxy");
                RDFNode createdR = solution.get("?created");
                LOGGER.debug(String
                        .format("Found resource %s with proxy %s created on %s", resourceR, proxyR, createdR));
                // resource must be a URI resource
                if (!resourceR.isURIResource()) {
                    LOGGER.warn("Resource " + resourceR + " is not a URI resource, it will be ignored");
                    continue;
                }
                URI rUri = URI.create(resourceR.asResource().getURI());
                // proxy is not yet aggregated, so an abstract resource
                if (!proxyR.isURIResource()) {
                    LOGGER.warn("Proxy " + proxy + " is not a URI resource, resource " + resourceR + " will be ignored");
                    continue;
                }
                if (!createdR.isLiteral()) {
                    LOGGER.warn("Created " + createdR + " is not a literal, resource " + resourceR + " will be ignored");
                    continue;
                }
                DateTime created2 = DateTime.parse(createdR.asLiteral().getString());

                // the creator is unknown
                Resource resource = new BundleResource(builder, rUri, bundleResearchObject, null, created2, zipFile);
                Proxy proxy2 = builder.buildProxy(URI.create(proxyR.asResource().getURI()), resource,
                    bundleResearchObject);
                resource.setProxy(proxy2);
                resources.add(resource);
            }
        } finally {
            qe.close();
        }
        return resources;
    }


    /**
     * Extract annotations based on the manifest in RDF and the contents of the ZIP bundle. The annotation bodies will
     * be special subclasses of resources that return their content from the ZIP archive.
     * 
     * Note that some annotations may be illegal for RODL, for example have abstract URIs as target.
     * 
     * @param bundleR
     *            the bundle RDF resource
     * @param bundleResearchObject
     *            abstract RO that will aggregate all resources
     * @param zipFile
     *            ZIP archive
     * @return a set of annotations
     */
    private Set<? extends Annotation> extractAnnotationsFromBundle(com.hp.hpl.jena.rdf.model.Resource bundleR,
            ResearchObject bundleResearchObject, ZipFile zipFile) {
        Set<Annotation> annotations = new HashSet<>();
        String queryString = String
                .format(
                    "SELECT ?annotation ?body ?target WHERE { <%s> <http://purl.org/wf4ever/bundle#hasAnnotation> ?annotation . ?annotation <http://www.w3.org/ns/oa#hasBody> ?body ; <http://www.w3.org/ns/oa#hasTarget> ?target . }",
                    bundleR.getURI());
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, bundleR.getModel());
        try {
            ResultSet results = qe.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                RDFNode annotationR = solution.get("?annotation");
                RDFNode bodyR = solution.get("?body");
                // FIXME there may be multiple targets per annotation
                RDFNode targetR = solution.get("?target");
                LOGGER.debug(String.format("Found annotation %s with target %s and body %s", annotationR, targetR,
                    bodyR));
                // annotation must be a resource but may be a blank node
                // FIXME need to check if the "URI" doesn't start with _
                URI aUri = annotationR.isURIResource() && !annotationR.asResource().getURI().startsWith("_") ? URI
                        .create(annotationR.asResource().getURI()) : URI.create(bundleR.getURI()).resolve(
                    "ann-" + UUID.randomUUID().toString());
                // body is not yet aggregated, so an abstract resource
                if (!bodyR.isURIResource()) {
                    LOGGER.warn("Annotation body " + bodyR + " is not a URI resource, annotation " + annotationR
                            + " will be ignored");
                    continue;
                }
                // the target may be some BundleResource so let's make the URI relative if possible
                URI bUri = bundleResearchObject.getUri().relativize(URI.create(bodyR.asResource().getURI()));
                Resource body = new BundleRDFResource(builder, bUri, bundleResearchObject, null, null, zipFile);
                if (!targetR.isURIResource()) {
                    LOGGER.warn("Annotation target " + bodyR + " is not a URI resource, annotation " + annotationR
                            + " will be ignored");
                    continue;
                }
                // the target may be the abstract RO or some BundleResource so let's make the URI relative if possible
                URI relativeTargetUri = bundleResearchObject.getUri().relativize(
                    URI.create(targetR.asResource().getURI()));
                Thing target = builder.buildThing(relativeTargetUri);
                Annotation annotation = builder.buildAnnotation(aUri, bundleResearchObject, body,
                    Collections.singleton(target));
                annotations.add(annotation);
            }
        } finally {
            qe.close();
        }
        return annotations;
    }


    /**
     * Extract folders based on the manifest in RDF and the contents of the ZIP bundle. The folders will have as content
     * special subclasses of resources that return their content from the ZIP archive.
     * 
     * Note that the RO URI in the bundle may be a folder itself but because it does not have a proxy, it will not be
     * returned by this method.
     * 
     * @param bundleR
     *            the bundle RDF resource
     * @param bundleResearchObject
     *            abstract RO that will aggregate all resources
     * @param zipFile
     *            ZIP archive
     * @return a set of folders
     */
    private Set<? extends Folder> extractFoldersFromBundle(com.hp.hpl.jena.rdf.model.Resource bundleR,
            ResearchObject bundleResearchObject, ZipFile zipFile) {
        Map<URI, BundleFolder> folders = new HashMap<>();
        String queryString = String
                .format(
                    "PREFIX ore: <%s> SELECT ?resource ?proxy ?created ?content WHERE { <%s> ore:aggregates ?resource . ?resource <http://purl.org/pav/createdOn> ?created ; <http://purl.org/wf4ever/bundle#hasProxy> ?proxy . ?content ore:proxyIn ?resource . }",
                    ORE.NAMESPACE, bundleR.getURI());
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, bundleR.getModel());
        try {
            ResultSet results = qe.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                RDFNode resourceR = solution.get("?resource");
                RDFNode proxyR = solution.get("?proxy");
                RDFNode createdR = solution.get("?created");
                RDFNode contentR = solution.get("?content");
                LOGGER.debug(String.format("Found folder %s with content %s and proxy %s created on %s", resourceR,
                    contentR, proxyR, createdR));
                // resource must be a URI resource
                if (!resourceR.isURIResource()) {
                    LOGGER.warn("Folder " + resourceR + " is not a URI resource, it will be ignored");
                    continue;
                }
                URI rUri = URI.create(resourceR.asResource().getURI());
                // content must be a URI resource
                if (!contentR.isURIResource()) {
                    LOGGER.warn("Folder content " + contentR + " is not a URI resource, it will be ignored");
                    continue;
                }
                URI cUri = URI.create(contentR.asResource().getURI());
                // proxy is not yet aggregated, so an abstract resource
                if (!proxyR.isURIResource()) {
                    LOGGER.warn("Proxy " + proxy + " is not a URI resource, resource " + resourceR + " will be ignored");
                    continue;
                }
                if (!createdR.isLiteral()) {
                    LOGGER.warn("Created " + createdR + " is not a literal, resource " + resourceR + " will be ignored");
                    continue;
                }
                DateTime created2 = DateTime.parse(createdR.asLiteral().getString());

                if (!folders.containsKey(rUri)) {
                    BundleFolder folder = new BundleFolder(builder, bundleResearchObject, rUri, null, created2);
                    folders.put(rUri, folder);
                    Proxy proxy2 = builder.buildProxy(URI.create(proxyR.asResource().getURI()), folder,
                        bundleResearchObject);
                    folder.setProxy(proxy2);
                }
                // the target may be some BundleResource so let's make the URI relative if possible
                URI relativeResourceUri = bundleResearchObject.getUri().relativize(cUri);
                Resource resource = new BundleResource(builder, relativeResourceUri, bundleResearchObject, null, null,
                        zipFile);
                folders.get(rUri).addFolderEntry(resource);
            }
        } finally {
            qe.close();
        }
        return new HashSet<>(folders.values());
    }


    @Override
    public void delete() {
        delete(true);
    }


    /**
     * Delete the RO bundle.
     * 
     * @param deleteNestedRO
     *            should the related nested RO be deleted as well
     */
    public void delete(boolean deleteNestedRO) {
        super.delete();
        if (deleteNestedRO) {
            ResearchObject nestedRO = ResearchObject.get(builder, uri);
            nestedRO.delete();
        }
    }
}
