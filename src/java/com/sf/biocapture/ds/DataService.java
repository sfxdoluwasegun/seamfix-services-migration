package com.sf.biocapture.ds;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import nw.orm.core.query.QueryModifier;
import nw.orm.core.query.QueryParameter;
import nw.orm.core.query.SQLModifier;
import nw.orm.core.service.Nworm;

import com.sf.biocapture.agl.integration.AgilityResponse;
import com.sf.biocapture.app.BsClazz;
import com.sf.biocapture.entity.EnrollmentRef;
import com.sf.biocapture.entity.Node;
import com.sf.biocapture.entity.audit.AgilityIntegrationLog;
import com.sf.biocapture.entity.enums.KycManagerRole;
import com.sf.biocapture.entity.security.KMRole;
import com.sf.biocapture.entity.security.KMUser;
import com.sf.biocapture.krm.ZonalSync;

import nw.orm.core.exception.NwormQueryException;

@TransactionAttribute(TransactionAttributeType.REQUIRED)
public abstract class DataService extends BsClazz {

	private String delimiter = "#s#x#";
	protected SimpleDateFormat defaultSdf = new SimpleDateFormat("yyyyMMddhhmmss");
	protected Nworm dbService;

	@PostConstruct
	public void init(){
		dbService = Nworm.getInstance();
	}

	public Nworm getDbService(){
		return this.dbService;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public Long countOf(Class<?> clazz, Criterion ...criterions){// jimmy solanke
		QueryModifier qm = new QueryModifier(clazz);
		qm.addProjection(Projections.rowCount());
		return dbService.getByCriteria(Long.class, qm, criterions);
	}

	public Long countRefs() {
		QueryModifier qm = new QueryModifier(EnrollmentRef.class);
		qm.addProjection(Projections.rowCount());

		return dbService.getByCriteria(Long.class, qm);
	}

	public List<EnrollmentRef> listEnrollmentRefs(Integer index) {
		QueryModifier qm = new QueryModifier(EnrollmentRef.class);
		qm.setPaginated(index, 300);
		qm.transformResult(false);

		return dbService.getListByCriteria(EnrollmentRef.class, qm);
	}

	public EnrollmentRef getEnrollmentRef(String ref) {
		return dbService.getByCriteria(EnrollmentRef.class, Restrictions.eq("code", ref));
	}

	public EnrollmentRef getEnrollmentRefByMac(String macAddress) {
		return dbService.getByCriteria(EnrollmentRef.class, Restrictions.eq("macAddress", macAddress));
	}

	public Node getNodeByMac(String macAddress) {
		return dbService.getByCriteria(Node.class, Restrictions.eq("macAddress", macAddress));
	}

	public Date getDateTime(int hourOfDay){
		Calendar cale = Calendar.getInstance();
		cale.set(cale.get(Calendar.YEAR), cale.get(Calendar.MONTH), cale.get(Calendar.DAY_OF_MONTH), hourOfDay, 0, 0);
		System.out.println(cale.getTime());
		return cale.getTime();
	}

	public Date getDay(int dayOfMonth){
		Calendar cale = Calendar.getInstance();
		cale.set(cale.get(Calendar.YEAR), cale.get(Calendar.MONTH), dayOfMonth, 0, 0, 0);
		System.out.println(cale.getTime());
		return cale.getTime();
	}

	public Date getMonth(int month){
		Calendar cale = Calendar.getInstance();
		cale.set(cale.get(Calendar.YEAR), month, 1, 0, 0, 0);
		System.out.println(cale.getTime());
		return cale.getTime();
	}

	public boolean update(Object obj){
		return dbService.update(obj);
	}

	public KMUser getKmUserByEmail(String email){
		return dbService.getByCriteria(KMUser.class, Restrictions.eq("emailAddress", email).ignoreCase());
	}

	public KMRole getKmUserRole(KycManagerRole role){
		return dbService.getByCriteria(KMRole.class, Restrictions.eq("role", role.name()));
	}

	public boolean save(Object item){
		return dbService.create(item) != null;
	}

	protected List<ZonalSync> getSyncCount(){

		String sql = "SELECT count(*) as \"syncs\", z.name as \"zone\" FROM SMS_ACTIVATION_REQUEST s, ENROLLMENT_REF e, NODE n, STATE st, ZONE z WHERE s.enrollment_ref = e.code "
				+ "AND e.mac_address = n.mac_address AND n.state_fk = st.id AND st.zone_fk = z.id AND s.receipt_timestamp BETWEEN :start AND :end GROUP BY z.name ";

//		String hql = "SELECT n.state.zone as zone, count(s) as syncs FROM SmsActivationRequest s, EnrollmentRef e, Node n WHERE s.receiptTimestamp BETWEEN :start AND :end and s.enrollmentRef = e.code and e.macAddress = n.macAddress";
		Date startDate = new Date(new Date().getTime() - 500000);
		Date endDate = new Date();
		logger.debug("Start Date: " + startDate + " End Date: " + endDate);
		List<ZonalSync> lsz = dbService.getBySQL(ZonalSync.class, sql, new SQLModifier(), QueryParameter.create("start", startDate), QueryParameter.create("end", endDate));
		logger.debug("Zonal Info returned: " + lsz.size());
		return lsz;
	}
	
	protected boolean isEmpty(String fieldVal){
		return fieldVal == null || (fieldVal != null && fieldVal.trim().isEmpty());
	}
	
	protected void logActivity(String requestXml, String responseXml, String code, 
			String description, String simSerial, String requestType, String msisdn){
            try {
		logger.debug("Logging agility integration request and response!");
		AgilityIntegrationLog log = new AgilityIntegrationLog();
		log.setRequestXml(requestXml);
		log.setResponseXml(responseXml);
		log.setResponseCode(code);
		log.setResponseDescription(description);
		log.setRequestType(requestType);
		log.setMsisdn(msisdn);
		log.setSimSerial(simSerial);
                dbService.create(log);
                logger.debug("Done logging esf calls to agility integration log...");
            } catch (NwormQueryException e) {
                logger.error("", e);
            }
	}
	
	protected String translateActivationStatus( String activationStatus ){
		//AC- Active, SP-Suspended, IA-Inactive, WP-Work Order Progress, PP- Pending Payments, PA=Pre-Active
		switch(activationStatus.toUpperCase()){
		case "AC" :
			return "ACTIVE";
		case "SP" :
			return "SUSPENDED";
		case "IA" :
			return "INACTIVE";
		case "WP" :
			return "WORK ORDER PROGRESS";
		case "PP" :
			return "PENDING PAYMENTS";
		case "PA" :
			return "PREACTIVE";
		default : 
			return "";                 
		}

	}

	protected AgilityResponse getFailureResponse(){
		AgilityResponse ar = new AgilityResponse();
		ar.setCode("-2");
		ar.setChildMsisdnCount(null);
		ar.setDescription("Unable to connect to remote service");
		ar.setValid(null);                
		return ar;
	}
	
	protected KMUser getUser(String userEmail){
		return dbService.getByCriteria(KMUser.class, Restrictions.eq("emailAddress", userEmail.trim()).ignoreCase());
	}
}
