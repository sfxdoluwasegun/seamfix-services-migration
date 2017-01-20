package com.sf.biocapture.ws.notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;

import nw.orm.core.exception.NwormQueryException;
import nw.orm.core.query.QueryModifier;

import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.sf.biocapture.ds.DataService;
import com.sf.biocapture.entity.KycBroadcast;
import com.sf.biocapture.entity.Node;
import com.sf.biocapture.entity.security.KMUser;
import com.sf.biocapture.ws.ResponseCodeEnum;

/**
 * 
 * @author Nnanna
 * @since 10/11/2016
 *
 */
@Stateless
public class NotificationDS extends DataService {
	
	public NotificationResponse getUserNotifications(NotificationRequest request){
		//check for missing email or mac address
		if(request.getEmail() == null || request.getMacAddress() == null){
			return new NotificationResponse(ResponseCodeEnum.INVALID_INPUT, "Email or mac address is missing");
		}
		
		KMUser user = getUser(request.getEmail());
		if(user == null){
			return new NotificationResponse(ResponseCodeEnum.FAILED_AUTHENTICATION, "User with email, " + request.getEmail() + ", not found");
		}else{
			//check if user is active
			if(!user.isActive()){
				return new NotificationResponse(ResponseCodeEnum.INACTIVE_ACCOUNT, "User is blacklisted");
			}
		}
		
		NotificationResponse resp = new NotificationResponse();
		
		try{
			//confirm that node exists
			Node node = getNodeByMac(request.getMacAddress());
			
			//load user notifications
			resp.setNotifications(formatNotifications(getNotifications(user, node, request.getLastMessageDate())));
			resp.setAgentEmail(request.getEmail());
			resp.setCode(ResponseCodeEnum.SUCCESS);
			resp.setDescription("Successfully retrieved user notifications");
		}catch(NwormQueryException ex){
			logger.error("Error in retrieving user notifications", ex);
			resp = new NotificationResponse(ResponseCodeEnum.ERROR, "Server Error");
		}
		return resp;
	}
	
	private List<NotificationData> formatNotifications(List<KycBroadcast> notifications){
		if(notifications == null){
			return null;
		}
		List<NotificationData> data = new ArrayList<NotificationData>();
		for(KycBroadcast broadcast : notifications){
			data.add(NotificationData.from(broadcast));
		}
		
		return data;
	}
	
	private List<KycBroadcast> getNotifications(KMUser user, Node node, Date lastMessageDate){
		Disjunction or = Restrictions.disjunction();
		or.add(Restrictions.eq("global", true));
		if(node != null){
			or.add(Restrictions.eq("targetNode.id", node.getId()));
		}
		if(user != null){
			or.add(Restrictions.eq("targetUser.pk", user.getPk()));
		}
		
		Conjunction and = Restrictions.conjunction();
		and.add(or);
		and.add(Restrictions.eq("active", true));
		and.add(Restrictions.eq("expired", false));
		if(lastMessageDate != null){ //client already has messages
			logger.debug("Latest message on the client: " + lastMessageDate);
			and.add(Restrictions.gt("createDate", lastMessageDate));
		}
		
		QueryModifier qm = new QueryModifier(KycBroadcast.class);
		qm.addOrderBy(Order.desc("lastModified"));
		
		return dbService.getListByCriteria(KycBroadcast.class, qm, and);
	}
}
