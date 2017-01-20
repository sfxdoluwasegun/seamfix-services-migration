
package com.sf.biocapture.email;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

public class SendEmail
{
  static boolean cacheEmailSendingParamters = false;

  public static void send(EmailBean emailBean) {
    HtmlEmail email = new HtmlEmail();
    email.setHostName("10.1.224.7"); //smtp.gmail.com");
    email.setSmtpPort(25); //465);
    String senderEmail = "noreply@seamfix.com";
   
//    email.setAuthenticator(new DefaultAuthenticator(senderEmail, ""));//n0reply"));
//    email.setSSL(true);
    email.setDebug(false);
    try
    {
      email.setFrom(senderEmail, "MTN KYC");
      email.setSubject(emailBean.getSubject());
      email.setHtmlMsg(emailBean.getMessage());
      email.addBcc(emailBean.getToEmail());
//      email.addTo(emailBean.getToEmail());
      
      if (emailBean.getAttachment() != null) {
        email.attach(emailBean.getAttachment());
      }
      
      if (emailBean.getCcEmails() != null) {
//        email.addCc(emailBean.getCcEmails());
        email.addBcc(emailBean.getCcEmails());
      }
      
 
      email.send();
    } catch (EmailException ex) {
      System.err.println(ex.toString());
    }
  }
}