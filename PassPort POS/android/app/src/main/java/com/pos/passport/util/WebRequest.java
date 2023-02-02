package com.pos.passport.util;

import android.util.Log;

import com.pos.passport.model.WebSetting;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

public class WebRequest {

    private final String mMPSExceptionString = "BPSWebRequest Error: %1$s";
    private final String mSOAPWrapper = "<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><%2$s xmlns=\"%1$s\">%3$s</%2$s></soap:Body></soap:Envelope>";
    private int mTimeout;
    private HashMap<String, String> mWSParameters;
    private String mWebMethodName = "";
    private URL mWebServiceURL;
    private final String mXMLNamespace = "http://TPISoft.com/SmartPayments/";

    public WebRequest(String paramString) throws Exception {
        setWebServiceURL(paramString);
        this.mWebMethodName = "";
        this.mTimeout = 20000;
        this.mWSParameters = new HashMap();
    }

    public void setWebMethodName(String webMethodName) {
        this.mWebMethodName = webMethodName.trim();
    }

    private void setWebServiceURL(String paramString) throws Exception {
        URL param = new URL(paramString.trim());
        if (param.getProtocol().equals("https")) {
            this.mWebServiceURL = param;
            System.out.println("kareem 5: " + paramString);
            return;
        }
    }

    public void setTimeout(int timeout) throws Exception {
        if (timeout > 0) {
            this.mTimeout = (timeout * 1000);
        } else {
            throw new Exception(String.format("MPSWebRequest Error: %1$s",
                    new Object[]{"Timeout value must be greater than 0"}));
        }
    }

    private String buildSOAPRequest() throws Exception {

        if (!this.mWSParameters.isEmpty()) {
            StringBuilder parameters = new StringBuilder();
            for (Entry<String, String> element : this.mWSParameters.entrySet()) {
                //parameters.append(String.format("<%1$s>%2$s</%1$s>",
                parameters.append(String.format("%2$s",
                        new Object[]{element.getKey(), element.getValue()}));
            }

			/*String xml = String
					.format("<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope " +
							"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
							"xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
							"xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">
							<soap:Body><%2$s xmlns=\"%1$s\">%3$s</%2$s></soap:Body></soap:Envelope>",
							new Object[] { "http://TPISoft.com/SmartPayments/",
									this.mWebMethodName, parameters.toString() });*/
            String xml = String.format("<?xml version=\"1.0\" encoding=\"utf-8\"?> " +
                            "<%2$s>%3$s</%2$s>",
                    new Object[]{"http://TPISoft.com/SmartPayments/",
                            this.mWebMethodName, parameters.toString()});

            System.out.println("web Request: " + xml);
            return xml;
        }

        throw new Exception(
                String.format(
                        "MPSWebRequest Error: %1$s",
                        new Object[]{"Cannot build SOAP request with no parameters"}));
    }

    public void addParameter(String paramString1, String paramString2)
            throws Exception
    {
        paramString1 = paramString1.trim();
        paramString2 = paramString2.trim();
        this.mWSParameters.put(paramString1, paramString2);
    }



    public String sendRequest() throws Exception {
        validateRequiredParameters();
        String responseData = "";
        boolean error = false;
        int k = 0;

        String buildSOAP = buildSOAPRequest();
        HttpsURLConnection conn = (HttpsURLConnection) this.mWebServiceURL.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setReadTimeout(this.mTimeout);
        conn.setConnectTimeout(this.mTimeout);
        conn.setUseCaches(false);
        conn.setDefaultUseCaches(false);
        conn.setRequestProperty("Content-Type", "application/xml");
        conn.setRequestProperty("Content-Length", String.valueOf(buildSOAP.length()));
        conn.setRequestProperty("SOAPAction","\"http://TPISoft.com/SmartPayments/" + this.mWebMethodName + "\"");
        conn.setRequestProperty("api-key", WebSetting.merchantID);
        conn.setRequestProperty("Connection", "Close");

        Log.v("Request Headers", conn.getRequestProperties().toString());
        OutputStreamWriter localOutputStreamWriter = new OutputStreamWriter(
                conn.getOutputStream());
        localOutputStreamWriter.write(buildSOAP);
        Log.v("Soap Response", buildSOAP);
        localOutputStreamWriter.flush();
        localOutputStreamWriter.close();

        int httpResponseCode =  conn.getResponseCode();
        BufferedReader rd;
        if (httpResponseCode != 200) {
            rd = new BufferedReader(new InputStreamReader(
                    conn.getErrorStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
        }

        String responseBuffer = "";
        while ((responseBuffer = rd.readLine()) != null) {
            responseData = responseData + responseBuffer;
        }
        rd.close();

        int start = 0;
        int end = 0;
        if (httpResponseCode != 200) {
            error = true;
			/*String returnparam = "faultstring";
			if (responseData.contains(returnparam)) {
				start = responseData.indexOf("<" + returnparam + ">")
						+ returnparam.length() + 2;
				end = responseData.indexOf("</" + returnparam + ">");
			}*/
        }/* else {
			
			String returnparam = this.mWebMethodName + "Result";
			start = responseData.indexOf("<" + returnparam + ">")
					+ returnparam.length() + 2;
			end = responseData.indexOf("</" + returnparam + ">");
		}*/
		/*responseData = responseData.substring(start, end).replace("&lt;", "<")
				.replace("&gt;", ">");*/
		/*if (error) {
			throw new Exception(String.format("MPSWebRequest Error: %1$s",
					new Object[] { responseData }));
		}*/

        System.out.println("send request: " + responseData);
        return responseData;

    }

    private void validateRequiredParameters() throws Exception {
        if (this.mWebMethodName.equals("")) {
            throw new Exception(String.format("BPSWebRequest Error: %1$s",
                    new Object[]{"WebMethodName is required"}));
        }
    }
}
