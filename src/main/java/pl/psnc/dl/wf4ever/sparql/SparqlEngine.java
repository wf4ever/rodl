package pl.psnc.dl.wf4ever.sparql;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.sms.QueryResult;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDB;

public class SparqlEngine {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(SparqlEngine.class);

    private Builder builder;

    /** SPARQL query syntax. */
    private static final Syntax SPARQL_SYNTAX = Syntax.syntaxARQ;


    public SparqlEngine(Builder builder)
            throws IOException {
        this.builder = builder;
        TDB.getContext().set(TDB.symUnionDefaultGraph, true);
    }


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
        if (SemanticMetadataService.SPARQL_JSON.equals(rdfFormat)) {
            outputFormat = SemanticMetadataService.SPARQL_JSON;
            ResultSetFormatter.outputAsJSON(out, qexec.execSelect());
        } else {
            outputFormat = SemanticMetadataService.SPARQL_XML;
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
            outputFormat = SemanticMetadataService.SPARQL_JSON;
            ResultSetFormatter.outputAsJSON(out, qexec.execAsk());
        } else {
            outputFormat = SemanticMetadataService.SPARQL_XML;
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

}
