package pl.psnc.dl.wf4ever.sparql;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.model.Builder;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateAction;

public class SparqlEngine {

    public static final RDFFormat SPARQL_XML = new RDFFormat("XML", "application/sparql-results+xml",
            Charset.forName("UTF-8"), "xml", false, false);

    public static final RDFFormat SPARQL_JSON = new RDFFormat("JSON", "application/sparql-results+json",
            Charset.forName("UTF-8"), "json", false, false);

    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(SparqlEngine.class);

    private Builder builder;

    /** SPARQL query syntax. */
    private static final Syntax SPARQL_SYNTAX = Syntax.syntaxARQ;


    public SparqlEngine(Builder builder) {
        this.builder = builder;
        TDB.getContext().set(TDB.symUnionDefaultGraph, true);
    }


    /**
     * Responses are a available in a range of different formats. The specific formats available depend on the type of
     * SPARQL query being executed. SPARQL defines four different types of query: CONSTRUCT, DESCRIBE, SELECT and ASK.
     * 
     * CONSTRUCT and DESCRIBE queries both return RDF graphs and so the usual range of RDF serializations are available,
     * including RDF/XML, RDF/JSON, Turtle, etc.
     * 
     * SELECT queries return a tabular result set, while ASK queries return a boolean value. Results from both of these
     * query types can be returned in either SPARQL XML Results Format or SPARQL JSON Results Format.
     * 
     * See also http://www.w3.org/TR/rdf-sparql-XMLres/
     * 
     * @param query
     * @param rdfFormat
     * @return
     */
    public QueryResult executeSparql(String queryS, RDFFormat rdfFormat) {
        boolean transactionStarted = builder.beginTransaction(ReadWrite.READ);
        try {
            if (queryS == null) {
                throw new NullPointerException("Query cannot be null");
            }
            Query query = null;
            try {
                query = QueryFactory.create(queryS, SPARQL_SYNTAX);
            } catch (Exception e) {
                throw new IllegalArgumentException("Wrong query syntax: " + e.getMessage());
            }

            switch (query.getQueryType()) {
                case Query.QueryTypeSelect:
                    return processSelectQuery(query, rdfFormat);
                case Query.QueryTypeConstruct:
                    return processConstructQuery(query, rdfFormat);
                case Query.QueryTypeDescribe:
                    return processDescribeQuery(query, rdfFormat);
                case Query.QueryTypeAsk:
                    return processAskQuery(query, rdfFormat);
                default:
                    return null;
            }
        } finally {
            builder.endTransaction(transactionStarted);
        }
    }


    private QueryResult processSelectQuery(Query query, RDFFormat rdfFormat) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RDFFormat outputFormat;
        QueryExecution qexec = QueryExecutionFactory.create(query, builder.getDataset());
        if (SPARQL_JSON.equals(rdfFormat)) {
            outputFormat = SPARQL_JSON;
            ResultSetFormatter.outputAsJSON(out, qexec.execSelect());
        } else {
            outputFormat = SPARQL_XML;
            ResultSetFormatter.outputAsXML(out, qexec.execSelect());
        }
        qexec.close();

        return new QueryResult(new ByteArrayInputStream(out.toByteArray()), outputFormat);
    }


    private QueryResult processAskQuery(Query query, RDFFormat rdfFormat) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RDFFormat outputFormat;
        QueryExecution qexec = QueryExecutionFactory.create(query, builder.getDataset());
        if ("application/sparql-results+json".equals(rdfFormat.getDefaultMIMEType())) {
            outputFormat = SPARQL_JSON;
            ResultSetFormatter.outputAsJSON(out, qexec.execAsk());
        } else {
            outputFormat = SPARQL_XML;
            ResultSetFormatter.outputAsXML(out, qexec.execAsk());
        }
        qexec.close();

        return new QueryResult(new ByteArrayInputStream(out.toByteArray()), outputFormat);
    }


    private QueryResult processConstructQuery(Query query, RDFFormat rdfFormat) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        QueryExecution qexec = QueryExecutionFactory.create(query, builder.getDataset());
        Model resultModel = qexec.execConstruct();
        qexec.close();

        RDFFormat outputFormat;
        if (RDFFormat.values().contains(rdfFormat)) {
            outputFormat = rdfFormat;
        } else {
            outputFormat = RDFFormat.RDFXML;
        }

        resultModel.write(out, outputFormat.getName().toUpperCase());
        return new QueryResult(new ByteArrayInputStream(out.toByteArray()), outputFormat);
    }


    private QueryResult processDescribeQuery(Query query, RDFFormat rdfFormat) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        QueryExecution qexec = QueryExecutionFactory.create(query, builder.getDataset());
        Model resultModel = qexec.execDescribe();
        qexec.close();

        RDFFormat outputFormat;
        if (RDFFormat.values().contains(rdfFormat)) {
            outputFormat = rdfFormat;
        } else {
            outputFormat = RDFFormat.RDFXML;
        }

        resultModel.removeNsPrefix("xml");

        resultModel.write(out, outputFormat.getName().toUpperCase());
        return new QueryResult(new ByteArrayInputStream(out.toByteArray()), outputFormat);
    }


    /**
     * Perform a SPARQL update operation.
     * 
     * @param queryS
     *            the update request
     */
    public void executeSparqlUpdate(String queryS) {
        boolean transactionStarted = builder.beginTransaction(ReadWrite.WRITE);
        try {
            if (queryS == null) {
                throw new NullPointerException("Query cannot be null");
            }
            GraphStore graphStore = GraphStoreFactory.create(builder.getDataset());
            UpdateAction.parseExecute(queryS, graphStore);
            builder.commitTransaction(transactionStarted);
        } finally {
            builder.endTransaction(transactionStarted);
        }
    }
}
