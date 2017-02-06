package com.ifair.common.interceptor.mybatis;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class PagingList<T> implements List<T> {
	private List<T> data;
	private PagingDevice pagingDevice;

	public PagingList(List<T> data, int count, PagingDevice pagingDevice) {
		this.data = data;
		this.pagingDevice = pagingDevice;
		this.pagingDevice.setCount(count);
	}

	public PagingList(List<T> data, PagingList pagingList) {
		this.data = data;
		this.pagingDevice = pagingList.getPagingDevice();
	}

	public PagingDevice getPagingDevice() {
		return pagingDevice;
	}

	public void setPagingDevice(PagingDevice pagingDevice) {
		this.pagingDevice = pagingDevice;
	}

	public boolean add(T t) {
		return data.add(t);
	}

	public int lastIndexOf(Object o) {
		return data.lastIndexOf(o);
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public <T1> T1[] toArray(T1[] a) {
		return data.toArray(a);
	}

	public boolean retainAll(Collection<?> c) {
		return data.retainAll(c);
	}

	public ListIterator<T> listIterator() {
		return data.listIterator();
	}

	public int indexOf(Object o) {
		return data.indexOf(o);
	}

	public void add(int index, T element) {
		data.add(index, element);
	}

	public boolean contains(Object o) {
		return data.contains(o);
	}

	public boolean removeAll(Collection<?> c) {
		return data.removeAll(c);
	}

	public T set(int index, T element) {
		return data.set(index, element);
	}

	public int size() {
		return data.size();
	}

	public T remove(int index) {
		return data.remove(index);
	}

	public ListIterator<T> listIterator(int index) {
		return data.listIterator(index);
	}

	public boolean remove(Object o) {
		return data.remove(o);
	}

	public Iterator<T> iterator() {
		return data.iterator();
	}

	public boolean containsAll(Collection<?> c) {
		return data.containsAll(c);
	}

	public boolean addAll(Collection<? extends T> c) {
		return data.addAll(c);
	}

	public T get(int index) {
		return data.get(index);
	}

	public List<T> subList(int fromIndex, int toIndex) {
		return data.subList(fromIndex, toIndex);
	}

	public boolean addAll(int index, Collection<? extends T> c) {
		return data.addAll(index, c);
	}

	public void clear() {
		data.clear();
	}

	public Object[] toArray() {
		return data.toArray();
	}
}
