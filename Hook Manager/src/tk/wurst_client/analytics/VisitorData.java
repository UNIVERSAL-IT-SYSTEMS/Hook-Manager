package tk.wurst_client.analytics;

import java.security.SecureRandom;

public class VisitorData
{
	private int visitorId;
	private long timestampFirst;
	private long timestampPrevious;
	private long timestampCurrent;
	private int visits;
	
	VisitorData(int visitorId, long timestampFirst, long timestampPrevious,
		long timestampCurrent, int visits)
	{
		this.visitorId = visitorId;
		this.timestampFirst = timestampFirst;
		this.timestampPrevious = timestampPrevious;
		this.timestampCurrent = timestampCurrent;
		this.visits = visits;
	}
	
	/**
	 * initializes a new visitor data, with new visitorid
	 */
	public static VisitorData newVisitor()
	{
		int visitorId = new SecureRandom().nextInt() & 0x7FFFFFFF;
		long now = now();
		return new VisitorData(visitorId, now, now, now, 1);
	}
	
	public static VisitorData newSession(int visitorId, long timestampfirst,
		long timestamplast, int visits)
	{
		long now = now();
		AnalyticsCookieManager.getCookie().last_launch = now;
		AnalyticsCookieManager.getCookie().launches = visits + 1;
		AnalyticsCookieManager.saveCookie();
		return new VisitorData(visitorId, timestampfirst, timestamplast, now,
			visits + 1);
	}
	
	public void resetSession()
	{
		long now = now();
		timestampPrevious = timestampCurrent;
		timestampCurrent = now;
		visits++;
	}
	
	private static long now()
	{
		long now = System.currentTimeMillis() / 1000L;
		return now;
	}
	
	public int getVisitorId()
	{
		return visitorId;
	}
	
	public long getTimestampFirst()
	{
		return timestampFirst;
	}
	
	public long getTimestampPrevious()
	{
		return timestampPrevious;
	}
	
	public long getTimestampCurrent()
	{
		return timestampCurrent;
	}
	
	public int getVisits()
	{
		return visits;
	}
	
}
