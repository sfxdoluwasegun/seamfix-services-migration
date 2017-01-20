package com.sf.biocapture.email;

import com.sf.biocapture.app.BsClazz;
import com.sf.biocapture.ws.license.LicenseRequestParams;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.stringtemplate.v4.ST;

public class EmailMethods extends BsClazz
{
	public static List<EmailBean> emails = new ArrayList();

	static SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM, dd  yyyy");

	public static enum EmailResponse
	{
		SUCCESS, INCOMPLETE_PARAMETERS, IO_EXCEPTION, NAMING_EXCEPTION, HIBERNATE_EXCEPTION;
	}

	public static EmailResponse sendLicenseRequest(LicenseRequestParams licenseParams)
	{
		if( (licenseParams == null) || (licenseParams.getAdminEmails() == null) || (licenseParams.getAdminEmails().length < 1) )
		{
			return EmailResponse.INCOMPLETE_PARAMETERS;
		}
		EmailResponse response = EmailResponse.SUCCESS;

		Map params = new HashMap();
		params.put("macAddress", licenseParams.getMacAddress());
		params.put("tagId", licenseParams.getTagId());
		params.put("agentName", licenseParams.getAgentName());
		params.put("agentEmail", licenseParams.getAgentEmail());
		params.put("formattedDate", licenseParams.getFormattedRequestTime());
		try
		{
			String message = generateMessage("license-request.stl", params);
			if ((message == null) || (message.trim().isEmpty())) {
				//            logger.debug("*****  Email parameters are not complete");
				return EmailResponse.INCOMPLETE_PARAMETERS;
			}

			EmailBean bean = new EmailBean(message, licenseParams.getAdminEmails(), null, "License Request");
			emails.add(bean);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return response;
	}


	private static String generateMessage(String templateFileName, Map<String, String> params) throws IOException {
		String line = "";
		InputStream is = EmailMethods.class.getClassLoader().getResourceAsStream("/mailtemplates/" + templateFileName);

		BufferedReader buffer = new BufferedReader( new InputStreamReader( is ) );

		StringBuilder builder = new StringBuilder();
		while ((line = buffer.readLine()) != null) {
                        String newLine = "\n";
			builder.append(line).append(newLine);
		}

		ST template = new ST(builder.toString(), '$', '$');
		Set<String> keys = params.keySet();
		keys.stream().forEach((k) -> {
			template.add(k, params.get(k));
		});

		String renderEmail = template.render();
		return renderEmail;
	}
}