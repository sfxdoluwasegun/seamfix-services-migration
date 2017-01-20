package com.sf.biocapture.email;

import org.apache.commons.mail.EmailAttachment;

public class EmailBean
{
  private String message;
  private String[] toEmail;
  private String[] ccEmails;
  private String subject;
  private EmailAttachment attachment;

  public EmailBean(String message, String[] toEmail, String[] ccEmails, String subject)
  {
    this.message = message;
    this.toEmail = toEmail;
    this.ccEmails = ccEmails;
    this.subject = subject;
  }

  public EmailBean(String message, String[] toEmail, String[] ccEmails, String subject, EmailAttachment attachment) {
    this(message, toEmail, ccEmails, subject);
    this.attachment = attachment;
  }

  public String getMessage()
  {
    return this.message;
  }

  public String[] getCcEmails() {
    return this.ccEmails;
  }

  public String getSubject() {
    return this.subject;
  }

  public String[] getToEmail() {
    return this.toEmail;
  }

  public EmailAttachment getAttachment() {
    return this.attachment;
  }
}