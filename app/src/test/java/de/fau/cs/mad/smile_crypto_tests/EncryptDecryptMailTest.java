package de.fau.cs.mad.smile_crypto_tests;

import android.os.AsyncTask;
import android.support.v4.util.Pair;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
/*import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.cms.RecipientId;
import org.spongycastle.cms.RecipientInformation;
import org.spongycastle.cms.RecipientInformationStore;
import org.spongycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.spongycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.spongycastle.cms.jcajce.JceKeyTransRecipientId;
import org.spongycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.mail.smime.SMIMEEnveloped;
import org.spongycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.spongycastle.mail.smime.SMIMEUtil;
import org.spongycastle.util.encoders.Base64;

import java.io.IOException;

import javax.mail.internet.MimeBodyPart;

import java.security.KeyPair;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
*/
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import de.fau.cs.mad.javax.activation.CommandMap;
import de.fau.cs.mad.javax.activation.MailcapCommandMap;
import de.fau.cs.mad.smile.android.encryption.DecryptMail;
import de.fau.cs.mad.smile.android.encryption.EncryptMail;
import de.fau.cs.mad.smile.android.encryption.SelfSignedCertificateCreator;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class EncryptDecryptMailTest {

    final String content = "Content for MIME-Messages, üäÖß, México 42!";

    @Test
    public void testDecryptMail() throws Exception {
        /*MimeMessage originalMimeMessage = new AsyncCreateMimeMessage().execute().get();
        Pair<PrivateKey, X509Certificate> cert = new SelfSignedCertificateCreator().createForTest();
        MimeMessage encryptedMimeMessage = encrypt(originalMimeMessage, cert.first, cert.second);

        MimeBodyPart decrypted = decrypt(encryptedMimeMessage, cert.first, cert.second);

        MimeMultipart multipart = (MimeMultipart) decrypted.getContent();
        BodyPart part = multipart.getBodyPart(0);

        String decryptedResult = (String) part.getContent();
        if(!decryptedResult.equals(content)){
            System.out.println("FAIL! Decrypted content is: " + decryptedResult);
            for(int i = 0; i < content.length(); i++) {
                if(content.charAt(i) == decryptedResult.charAt(i))
                    System.out.println(i + ". " + content.charAt(i) + "==" + decryptedResult.charAt(i));
                else
                    System.out.println(i + ". " + content.charAt(i) + "!=" + decryptedResult.charAt(i));
            }
            fail();
        }*/

    }

    private MimeMessage encrypt(MimeMessage originalMimeMessage, PrivateKey key, X509Certificate cert)  throws Exception{
        System.out.println("Start encrypt.");

        EncryptMail encryptMail = new EncryptMail();
        return encryptMail.encryptMessage(originalMimeMessage, cert);
    }

    private MimeBodyPart decrypt(MimeMessage mimeMessage, PrivateKey key, X509Certificate cert) throws Exception {
        System.out.println("Start decrypt.");

        DecryptMail decryptMail = new DecryptMail();

        return decryptMail.decryptMail(mimeMessage, key, cert);
    }

    private class AsyncCreateMimeMessage extends AsyncTask<Void, Void, MimeMessage> {
        protected MimeMessage doInBackground(Void... params) {
            try {
                MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
                mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
                mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
                mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
                mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
                mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");

                Properties props = System.getProperties();
                Session session = Session.getDefaultInstance(props, null);
                MimeMessage mimeMessage = new MimeMessage(session);

                MimeMultipart multipart = new MimeMultipart("alternative");
                InternetHeaders headers = new InternetHeaders();
                headers.addHeader("Content-Type", "text/plain; charset=utf-8");
                headers.addHeader("Content-Transfer-Encoding", "quoted-printable");
                MimeBodyPart bodyPart = new MimeBodyPart(headers, content.getBytes());
                multipart.addBodyPart(bodyPart);
                mimeMessage.setContent(multipart);
                mimeMessage.saveChanges();

                mimeMessage.addFrom(new Address[]{new InternetAddress("fixmymail@t-online.de")});
                mimeMessage.addRecipients(Message.RecipientType.TO, "SMile@MAD.de");
                mimeMessage.saveChanges();
                return mimeMessage;

            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }
            return null;
        }
    }
}