package android.utils;

import java.util.LinkedList;
import java.util.Queue;

public class LastSeenQueue<T> extends LinkedList<T>{
	private final int MAX_SIZE;

	public LastSeenQueue(int mAX_SIZE) {
		super();
		MAX_SIZE = mAX_SIZE;
	}


	@Override
	public boolean offer(T e) {
		if (super.size() >= MAX_SIZE) super.remove();
		if (super.contains(e)) super.remove(e);
		return super.offer(e);
	}


}
