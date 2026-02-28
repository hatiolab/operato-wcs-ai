package xyz.elidom.rabbitmq.service.model;

import java.util.List;

/**
 * ???
 * @author yang
 *
 */
public class QueueSearch {
	private int filteredCount;
	private int itemCount;
	private int page;
	private int pageCount;
	private int pageSize;
	private int totalCount;
	private List<Queue> items;
	
	public int getFilteredCount() {
		return filteredCount;
	}
	public void setFilteredCount(int filteredCount) {
		this.filteredCount = filteredCount;
	}
	public int getItemCount() {
		return itemCount;
	}
	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getPageCount() {
		return pageCount;
	}
	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	public List<Queue> getItems() {
		return items;
	}
	public void setItems(List<Queue> items) {
		this.items = items;
	}

	
}
