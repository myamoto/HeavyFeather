package org.toolup.network.http.json;

import org.toolup.network.http.HttpResp;

public class HttpRespW <T>{
	
	private HttpResp resp;
	private T entityResp;
	public HttpResp getResp() {
		return resp;
	}
	public HttpRespW<T> setResp(HttpResp resp) {
		this.resp = resp;
		return this;
	}
	public T getEntityResp() {
		return entityResp;
	}
	public HttpRespW<T> setEntityResp(T entityResp) {
		this.entityResp = entityResp;
		return this;
	}
	@Override
	public String toString() {
		return "HttpRespW [resp=" + resp + ", entityResp=" + entityResp + "]";
	}
	
	

}
