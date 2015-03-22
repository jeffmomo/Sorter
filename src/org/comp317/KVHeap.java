package org.comp317;



/**
 * Heap implementation based on 0-based Array
 *
 *
 */

// MinHeap
public class KVHeap
{
	private int _size = 0;
	private KVPair<Integer, String>[] _heapBase;

	//
// Constructors
//
	public KVHeap(int capacity)
	{
		_heapBase = new KVPair[capacity];
	}

	//
// Public Methods
//
	public void insert(KVPair<Integer,String> item)
	{
		_heapBase[_size++] = item;

		// Upheap
		for(int position = _size - 1; position > 0;)
		{
			int parentPosition = (position - 1) / 2;
			if(item.value.compareTo(_heapBase[parentPosition].value) > 0)
				return;

			swap(position, position = parentPosition);
		}
	}

	public KVPair<Integer,String> get()
	{
		if(_heapBase[0] == null)
			return null;

		KVPair<Integer,String> smallest = _heapBase[0];

		_heapBase[0] = _heapBase[--_size];

		downHeapFrom(0);
		return smallest;
	}

	public KVPair<Integer,String> replace(KVPair<Integer,String> item)
	{
		KVPair<Integer,String> smallest = _heapBase[0];

		_heapBase[0] = item;
		downHeapFrom(0);

		return smallest;
	}

	public KVPair<Integer,String> peek()
	{
		return _heapBase[0];
	}
	public int size()
	{
		return _size;
	}
	public int capacity()
	{
		return _heapBase.length;
	}


	//
// Private Methods
//
	private void downHeapFrom(int fromPosition)
	{
		int position = fromPosition;

		for(;;)
		{
			int d = position * 2 + 1;
			if(d >= _size)
				return;

			if(d + 1 < _size && _heapBase[d].value.compareTo(_heapBase[d + 1].value) > 0)
				d++;

			if(_heapBase[position].value.compareTo(_heapBase[d].value) > 0)
			{
				swap(position, d);
				position = d;
			}
			else
				return;
		}
	}
	private void swap(int idxA, int idxB)
	{
		KVPair<Integer,String> tmp = _heapBase[idxA];

		_heapBase[idxA] = _heapBase[idxB];
		_heapBase[idxB] = tmp;
	}


}
