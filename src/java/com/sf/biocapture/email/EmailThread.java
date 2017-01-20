package com.sf.biocapture.email;

import java.util.ConcurrentModificationException;
import java.util.ListIterator;

public class EmailThread
  implements Runnable
{
  @Override
  public void run()
  {
    synchronized (this)
    {
      try
      {
        for (ListIterator beanIter = EmailMethods.emails.listIterator(); beanIter.hasNext(); )
        {
          EmailBean bean = (EmailBean)beanIter.next();
          SendEmail.send(bean);
          super.notify();
          beanIter.remove();
        }
      }
      catch (ConcurrentModificationException cm) {
        System.out.println("Concurrent Modification Exception occured while sending emails..." + cm);
      }
    }
  }
}