/**
 * Copyright 2011-2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.elidom.dbist.dml;

/**
 * @author Steve M. Jung
 * @since 2011. 6. 2. (version 0.0.1)
 */
public class Order {
	
	private String name;
	private String field;
	private Boolean ascending;
	private Boolean desc;
	
	public Order(String field, Boolean ascending) {
		this.field = field;
		this.name = field;
		this.ascending = ascending;
		this.desc = !ascending;
	}
	
	public String getField() {
		return this.field == null ? this.name : this.field;
	}
	
	public void setField(String field) {
		this.field = field;
		this.name = field;
	}
	
	public Boolean isAscending() {
		return this.ascending != null ? this.ascending : (this.desc != null ? !this.desc : true); 
	}
	
	public void setAscending(Boolean ascending) {
		this.ascending = ascending;
		this.desc = !ascending;
	}

	public String getName() {
		return this.name == null ? this.field : this.name;
	}

	public void setName(String name) {
		this.name = name;
		this.field = name;
	}

	public Boolean isDesc() {
		return this.desc != null ? !this.desc : (this.ascending != null ? this.ascending : false);
	}

	public void setDesc(Boolean desc) {
		this.desc = desc;
		this.ascending = !desc;
	}
}
