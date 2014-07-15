package im.delight.faceless;

/**
 * Copyright (C) 2014 www.delight.im <info@delight.im>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see {http://www.gnu.org/licenses/}.
 */

import im.delight.android.webrequest.WebRequest;
import org.apache.http.client.methods.HttpRequestBase;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

/** WebRequest subclass that sets the User-Agent and X-REST-Signature header to custom values matched on the server */
public class APIRequest extends WebRequest {
	
	protected static final String HTTP_HEADER_API_SIGNATURE = "X-Method-Signature";
	protected static final String HTTP_HEADER_API_TIMESTAMP = "X-Method-Timestamp";
	protected String mRestPath;
	protected String mTimestamp;

	public APIRequest(Context context) {
		// try to receive compressed content
		askForGzip(true);
		// build user agent string by concatenation as String.format(...) changes results based on the current Locale
		mUserAgent = Config.CLIENT_VERSION_PREFIX+getClientVersionID(context);
		// set the timestamp that will be sent in the headers and in the signature hash
		mTimestamp = String.valueOf(System.currentTimeMillis() / 1000L);
	}
	
	@Override
	public WebRequest to(String restPath) {
		mRestPath = restPath;
		return super.to(Config.API_BASE_URL+restPath);
	}
	
	@Override
	public WebRequest asUserAgent(String userAgent) {
		throw new RuntimeException("Set the User-Agent header once when calling the constructor and don't change it later");
	}
	
	@Override
	protected void addCustomHTTPHeaders(HttpRequestBase request) {
		request.setHeader(HTTP_HEADER_API_SIGNATURE, getRequestSignature());
		request.setHeader(HTTP_HEADER_API_TIMESTAMP, mTimestamp);
	}
	
	protected String getRequestSignature() {
		return Global.Crypto.sign(getRequestIdentifier());
	}
	
	protected String getRequestIdentifier() {
		return mRestPath+"#"+httpBuildQuery(mParams, mCharset)+"#"+mTimestamp+"#"+(mUsername != null ? mUsername : "");
	}
	
	protected static int getClientVersionID(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		}
		catch (NameNotFoundException e) {
			throw new RuntimeException("Could not detect client version ID");
		}
	}

}