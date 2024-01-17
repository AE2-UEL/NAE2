package co.neeve.nae2.common.helpers;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class ObjectIndexableLinkedOpenHashSet<T> implements Set<T> {
	private final ObjectArrayList<T> indexes = new ObjectArrayList<>();
	private final ObjectLinkedOpenHashSet<T> values = new ObjectLinkedOpenHashSet<>();

	@Override
	public int size() {
		return this.indexes.size();
	}

	@Override
	public boolean isEmpty() {
		return this.indexes.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return this.values.contains(o);
	}

	@NotNull
	@Override
	public Iterator<T> iterator() {
		return this.values.iterator();
	}

	@NotNull
	@Override
	public Object @NotNull [] toArray() {
		return this.values.toArray();
	}

	@NotNull
	@Override
	public <T1> T1 @NotNull [] toArray(T1 @NotNull [] a) {
		return this.values.toArray(a);
	}

	@Override
	public boolean add(T t) {
		if (this.values.add(t)) {
			this.indexes.add(t);
			return true;
		}
		return false;
	}

	@Override
	public boolean remove(Object o) {
		if (this.values.remove(o)) {
			this.indexes.remove(o);
			return true;
		}
		return false;
	}

	@Override
	public boolean containsAll(@NotNull Collection<?> c) {
		return this.values.containsAll(c);
	}

	@Override
	public boolean addAll(@NotNull Collection<? extends T> c) {
		if (this.values.addAll(c)) {
			this.indexes.addAll(c);
			return true;
		}
		return false;
	}

	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		if (this.values.retainAll(c)) {
			this.indexes.retainAll(c);
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		if (this.values.removeAll(c)) {
			this.indexes.removeAll(c);
			return true;
		}
		return false;
	}

	@Override
	public void clear() {
		this.values.clear();
		this.indexes.clear();
	}

	public T getByIndex(int index) {
		return this.indexes.get(index);
	}

	public void makeFirst(T o) {
		if (this.remove(o)) {
			if (this.values.addAndMoveToFirst(o)) {
				this.indexes.add(0, o);
			}
		}
	}
}
