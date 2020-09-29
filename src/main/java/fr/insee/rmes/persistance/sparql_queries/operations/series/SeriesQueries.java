package fr.insee.rmes.persistance.sparql_queries.operations.series;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;

public class SeriesQueries {

	private SeriesQueries() {
		throw new IllegalStateException("Utility class");
	}

	private static StringBuilder variables;
	private static StringBuilder whereClause;
	static Map<String, Object> params;

	public static String seriesQuery() {
		return "SELECT DISTINCT ?id ?label (group_concat(?altLabelLg1;separator=' || ') as ?altLabel) \n"
				+ "WHERE { GRAPH <"+Config.OPERATIONS_GRAPH+"> { \n"
				+ "?series a insee:StatisticalOperationSeries . \n" + "?series skos:prefLabel ?label . \n"
				+ "FILTER (lang(?label) = '" + Config.LG1 + "') \n"
				+ "BIND(STRAFTER(STR(?series),'/operations/serie/') AS ?id) . \n"
				+ "OPTIONAL{?series skos:altLabel ?altLabelLg1 . " + "FILTER (lang(?altLabelLg1) = '" + Config.LG1
				+ "') } \n " + "}} \n" + "GROUP BY ?id ?label \n" + "ORDER BY ?label ";
	}

	public static String seriesWithSimsQuery() {
		return "SELECT DISTINCT ?id ?label ?idSims (group_concat(?altLabelLg1;separator=' || ') as ?altLabel) \n"
				+ "WHERE { \n" + "GRAPH <"+Config.OPERATIONS_GRAPH+"> { \n"
				+ "?series a insee:StatisticalOperationSeries . \n" + "?series skos:prefLabel ?label . \n"
				+ "FILTER (lang(?label) = '" + Config.LG1 + "') \n"
				+ "BIND(STRAFTER(STR(?series),'/operations/serie/') AS ?id) . \n"
				+ "OPTIONAL{ ?report rdf:type sdmx-mm:MetadataReport ." + " ?report sdmx-mm:target ?series "
				+ " BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) . \n" + "} \n"
				+ "OPTIONAL{?series skos:altLabel ?altLabelLg1 . " + "FILTER (lang(?altLabelLg1) = '" + Config.LG1
				+ "') } \n " + "}" + "} \n" + "GROUP BY ?id ?label ?idSims \n" + "ORDER BY ?label ";
	}

	public static String oneSeriesQuery(String id) {
		variables = null;
		whereClause = null;
		getSimpleAttr(id);
		getCodesLists();
		getValidationState();

		return "SELECT " + variables.toString() + " WHERE {  \n" + whereClause.toString() + "} \n" + "LIMIT 1";
	}

	public static String getSeriesForSearch() {
		variables = null;
		whereClause = null;
		getSimpleAttr(null);
		getCodesLists();

		return "SELECT DISTINCT " + variables.toString() + " WHERE {  \n" + whereClause.toString() + "} \n";
	}

	public static String seriesLinks(String idSeries, IRI linkPredicate) {
		return "SELECT ?id ?typeOfObject ?labelLg1 ?labelLg2 \n" + "WHERE { \n" + "?series <" + linkPredicate
				+ "> ?uriLinked . \n" + "?uriLinked skos:prefLabel ?labelLg1 . \n" 
				+ "FILTER (lang(?labelLg1) = '"+ Config.LG1 + "') . \n" 
				+ "OPTIONAL {?uriLinked skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "')} . \n" + "?uriLinked rdf:type ?typeOfObject . \n"
				+ "BIND(REPLACE( STR(?uriLinked) , '(.*/)(\\\\w+$)', '$2' ) AS ?id) . \n"

				+ "FILTER(STRENDS(STR(?series),'/operations/serie/" + idSeries + "')) . \n"

				+ "} \n" + "ORDER BY ?labelLg1";
	}

	public static String getOperations(String idSeries) {
		return "SELECT ?id ?labelLg1 ?labelLg2 \n" 
				+ " FROM <"+Config.OPERATIONS_GRAPH+"> \n" 
				+ "WHERE { \n"
				+ "?series dcterms:hasPart ?uri . \n" + "?uri skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') . \n" + "?uri skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "') . \n"
				+ "BIND(STRAFTER(STR(?uri),'/operations/operation/') AS ?id) . \n"

				+ "FILTER(STRENDS(STR(?series),'/operations/serie/" + idSeries + "')) . \n" + "}" + " ORDER BY ?id";
	}

	public static String getGeneratedWithOld(String idSeries) {
		return "SELECT ?id ?typeOfObject ?labelLg1 ?labelLg2 \n" 
				+ "WHERE { \n"

				+ "?uri prov:wasGeneratedBy ?series . \n" + "?uri skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') . \n" + "?uri skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "') . \n" + "?uri rdf:type ?typeOfObject . \n"

				+ "BIND(REPLACE( STR(?uri) , '(.*/)(\\\\w+$)', '$2' ) AS ?id) . \n"

				+ "FILTER(STRENDS(STR(?series),'/operations/serie/" + idSeries + "')) . \n" + "}" + " ORDER BY ?id";
	}

	public static String getMultipleOrganizationsOld(String idSeries, IRI linkPredicate) {
		return "SELECT ?id ?labelLg1 ?labelLg2\n" + "WHERE { \n" + "?series <" + linkPredicate + "> ?uri . \n"
				+ "?uri dcterms:identifier  ?id . \n" + "?uri skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') . \n"
				+ "OPTIONAL {?uri skos:prefLabel ?labelLg2 . \n" + "FILTER (lang(?labelLg2) = '" + Config.LG2
				+ "')} . \n"

				+ "FILTER(STRENDS(STR(?series),'/operations/serie/" + idSeries + "')) . \n"

				+ "} \n" + "ORDER BY ?id";
	}

	public static String getFamilyOld(String idSeries) {

		return "SELECT ?id ?labelLg1 ?labelLg2 \n" + " FROM <"+Config.OPERATIONS_GRAPH+"> \n" + "WHERE { \n"

				+ "?family dcterms:hasPart ?series . \n" + "?family skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') . \n" + "?family skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "') . \n"
				+ "BIND(STRAFTER(STR(?family),'/famille/') AS ?id) . \n"

				+ "FILTER(STRENDS(STR(?series),'/operations/serie/" + idSeries + "')) . \n" + "}"

		;
	}

	public static String getOwnerOld(String uris) {
		return "SELECT ?owner { \n" 
				+ "?series dcterms:publisher ?owner . \n" 
				+ "VALUES ?series { " + uris + " } \n"
				+ "}";
	}

	public static String getCreatorsBySeriesUriOld(String uris) {
		return "SELECT ?creators { \n" + "?series dc:creator ?creators . \n" 
				+ "VALUES ?series { " + uris
				+ " } \n" + "}";
	}

	public static String getCreatorsByIdOld(String id) {
		return "SELECT ?creators\n" 
				+ "WHERE { GRAPH <"+Config.OPERATIONS_GRAPH+"> { \n"
				+ "?series a insee:StatisticalOperationSeries . \n" 
				+ " FILTER(STRENDS(STR(?series),'/operations/serie/"+ id + "')) . \n" 
				+ "?series dc:creator ?creators  . \n" + "} }";
	}
	
	/**
	 * return publishers id (publishers are organizations)
	 * @param id
	 * @return
	 */
	public static String getPublishersOld(String id) {
		return "SELECT distinct ?publishers \n" 
				
				+ "FROM <"+Config.OPERATIONS_GRAPH+"> "
				+ "FROM <"+Config.ORGANIZATIONS_GRAPH+"> "
				+ "FROM <"+Config.ORG_INSEE_GRAPH+"> "
				+ "WHERE { \n"
					+ "?series a insee:StatisticalOperationSeries . \n" 
					+ "?series dcterms:publisher ?uri  . \n" 
					+ "?uri dcterms:identifier  ?publishers . \n" 
					+ " FILTER(STRENDS(STR(?series),'/operations/serie/"+ id + "')) . \n" 
				+ "} ";
	}
	
	
	private static void getSimpleAttr(String id) {

		if (id != null) {
			addClauseToWhereClause(" FILTER(STRENDS(STR(?series),'/operations/serie/" + id + "')) . \n");
		} else {
			addClauseToWhereClause("?series a insee:StatisticalOperationSeries .");
			addClauseToWhereClause("BIND(STRAFTER(STR(?series),'/operations/serie/') AS ?id) . ");
		}

		addVariableToList("?id ?prefLabelLg1 ?prefLabelLg2 ");
		addClauseToWhereClause("?series skos:prefLabel ?prefLabelLg1 \n");
		addClauseToWhereClause("FILTER (lang(?prefLabelLg1) = '" + Config.LG1 + "')  \n ");
		addClauseToWhereClause("OPTIONAL{?series skos:prefLabel ?prefLabelLg2 \n");
		addClauseToWhereClause("FILTER (lang(?prefLabelLg2) = '" + Config.LG2 + "') } \n ");

		addVariableToList(" ?altLabelLg1 ?altLabelLg2 ");
		addOptionalClause("skos:altLabel", "?altLabel");

		addVariableToList(" ?abstractLg1 ?abstractLg2 ");
		addOptionalClause("dcterms:abstract", "?abstract");

		addVariableToList(" ?historyNoteLg1 ?historyNoteLg2 ");
		addOptionalClause("skos:historyNote", "?historyNote");

		addVariableToList(" ?idSims ");
		addGetSimsId();
	}

	private static void addOptionalClause(String predicate, String variableName) {
		addClauseToWhereClause("OPTIONAL{?series " + predicate + " " + variableName + "Lg1 \n");
		addClauseToWhereClause("FILTER (lang(" + variableName + "Lg1) = '" + Config.LG1 + "') } \n ");
		addClauseToWhereClause("OPTIONAL{?series " + predicate + " " + variableName + "Lg2 \n");
		addClauseToWhereClause("FILTER (lang(" + variableName + "Lg2) = '" + Config.LG2 + "') } \n ");
	}

	private static void addGetSimsId() {
		addClauseToWhereClause(
				"OPTIONAL{ ?report rdf:type sdmx-mm:MetadataReport ." + " ?report sdmx-mm:target ?series "
						+ " BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) . \n" + "} \n");
	}

	private static void getCodesLists() {
		addVariableToList(" ?typeCode ?typeList ");
		addClauseToWhereClause("OPTIONAL {?series dcterms:type ?type . \n" + "?type skos:notation ?typeCode . \n"
				+ "?type skos:inScheme ?typeCodeList . \n" + "?typeCodeList skos:notation ?typeList . \n" + "}   \n");

		addVariableToList(" ?accrualPeriodicityCode ?accrualPeriodicityList ");
		addClauseToWhereClause("OPTIONAL {?series dcterms:accrualPeriodicity ?accrualPeriodicity . \n"
				+ "?accrualPeriodicity skos:notation ?accrualPeriodicityCode . \n"
				+ "?accrualPeriodicity skos:inScheme ?accrualPeriodicityCodeList . \n"
				+ "?accrualPeriodicityCodeList skos:notation ?accrualPeriodicityList . \n" + "}   \n");
	}


	private static void getValidationState() {
		addVariableToList(" ?validationState ");
		addClauseToWhereClause("OPTIONAL {?series insee:validationState ?validationState . \n" + "}   \n");
	}

	private static void addVariableToList(String variable) {
		if (variables == null) {
			variables = new StringBuilder();
		}
		variables.append(variable);
	}

	private static void addClauseToWhereClause(String clause) {
		if (whereClause == null) {
			whereClause = new StringBuilder();
		}
		whereClause.append(clause);
	}
	
	
  //////////////////////////
 //   Using .flth files  //
//////////////////////////
	

	private static final String ID_SERIES = "ID_SERIES";
	private static final String URI_SERIES = "URI_SERIES";
	private static final String ORGANIZATIONS_GRAPH = "ORGANIZATIONS_GRAPH";
	private static final String OPERATIONS_GRAPH = "OPERATIONS_GRAPH";
	private static final String ORG_INSEE_GRAPH = "ORG_INSEE_GRAPH";
	private static final String LINK_PREDICATE = "LINK_PREDICATE";
	
	private static void initParams() {
		params = new HashMap<>();
		params.put("LG1", Config.LG1);
		params.put("LG2", Config.LG2);
		params.put(OPERATIONS_GRAPH, Config.OPERATIONS_GRAPH);
	}
	
	private static String buildSeriesRequest(String fileName, Map<String, Object> params) throws RmesException  {
		return FreeMarkerUtils.buildRequest("operations/series/", fileName, params);
	}
	
	/**
	 * @param idSeries
	 * @return String
	 * @throws RmesException
	 */	
	public static String getFamily(String idSeries) throws RmesException {
		if (params==null) {initParams();}
		params.put(ID_SERIES, idSeries);
		return buildSeriesRequest("getSeriesFamilyQuery.ftlh", params);	
	}
	
	/**
	 * @param uriSeries
	 * @return String
	 * @throws RmesException
	 */	
	public static String getOwner(String uriSeries) throws RmesException {
		if (params==null) {initParams();}
		params.put(URI_SERIES, uriSeries);
		return buildSeriesRequest("getSeriesOwnerQuery.ftlh", params);	
	}
	
	/**
	 * @param uriSeries
	 * @return String
	 * @throws RmesException
	 */	
	public static String getCreatorsBySeriesUri(String uriSeries) throws RmesException {
		if (params==null) {initParams();}
		params.put(URI_SERIES, uriSeries);
		return buildSeriesRequest("getSeriesCreatorsByUriQuery.ftlh", params);	
	}
	
	/**
	 * @param idSeries
	 * @return String
	 * @throws RmesException
	 */	
	public static String getCreatorsById(String idSeries) throws RmesException {
		if (params==null) {initParams();}
		params.put(ID_SERIES, idSeries);
		return buildSeriesRequest("getSeriesCreatorsByIdQuery.ftlh", params);	
	}
	
	/**
	 * @param idSeries
	 * @return String
	 * @throws RmesException
	 */	
	public static String getPublishers(String idSeries) throws RmesException {
		if (params==null) {initParams();}
		params.put(ID_SERIES, idSeries);
		params.put(ORGANIZATIONS_GRAPH, Config.ORGANIZATIONS_GRAPH);
		params.put(ORG_INSEE_GRAPH, Config.ORG_INSEE_GRAPH);
		return buildSeriesRequest("getSeriesPublishersQuery.ftlh", params);	
	}
	
	/**
	 * @param idSeries, linkPredicate
	 * @return String
	 * @throws RmesException
	 */	
	public static String getMultipleOrganizations(String idSeries, IRI linkPredicate) throws RmesException {
		if (params==null) {initParams();}
		params.put(ID_SERIES, idSeries);
		params.put(LINK_PREDICATE, linkPredicate);
		return buildSeriesRequest("getSeriesMultipleOrganizations.ftlh", params);	
	}
	
	/**
	 * @param idSeries
	 * @return String
	 * @throws RmesException
	 */	
	public static String getGeneratedWith(String idSeries) throws RmesException {
		if (params==null) {initParams();}
		params.put(ID_SERIES, idSeries);
		return buildSeriesRequest("getSeriesGeneratedWithQuery.ftlh", params);	
	}
	
}
