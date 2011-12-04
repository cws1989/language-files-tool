package langfiles.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Sorted ArrayList.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class SortedArrayList<E extends Comparable<? super E>> extends ArrayList<E> {

  private static final long serialVersionUID = 1L;
  private boolean isSorting = false;
  private final Object sortingLock = new Object();

  public SortedArrayList() {
    super();
  }

  public SortedArrayList(Collection<? extends E> c) {
    super(c);
    sortList();
  }

  public SortedArrayList(int initialCapacity) {
    super(initialCapacity);
  }

  @Override
  public boolean add(E e) {
    boolean returnValue = super.add(e);
    sortList();
    return returnValue;
  }

  @Override
  public void add(int index, E element) {
    super.add(index, element);
    sortList();
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    boolean returnValue = super.addAll(c);
    sortList();
    return returnValue;
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    boolean returnValue = super.addAll(index, c);
    sortList();
    return returnValue;
  }

  @Override
  public E remove(int index) {
    E oldValue = super.remove(index);
    sortList();
    return oldValue;
  }

  @Override
  public boolean remove(Object o) {
    boolean returnValue = super.remove(o);
    sortList();
    return returnValue;
  }

  @Override
  protected void removeRange(int fromIndex, int toIndex) {
    super.removeRange(fromIndex, toIndex);
    sortList();
  }

  @Override
  public E set(int index, E element) {
    E returnValue = super.set(index, element);
    sortList();
    return returnValue;
  }

  private void sortList() {
    synchronized (sortingLock) {
      if (isSorting) {
        return;
      }
      isSorting = true;
    }
    Collections.sort(this);
    synchronized (sortingLock) {
      isSorting = false;
    }
  }
}
