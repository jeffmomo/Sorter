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
	private int[] _heapBaseK;
	private String[] _heapBaseV;

	//
// Constructors
//
	public KVHeap(int capacity)
	{
		_heapBaseK = new int[capacity];
		_heapBaseV = new String[capacity];
	}

	//
// Public Methods
//
	public void insert(KVPair<Integer,String> item)
	{
		_heapBaseK[_size] = item.key;
		_heapBaseV[_size] = item.value;
		_size++;

		// Upheap
		for(int position = _size - 1; position > 0;)
		{
			int parentPosition = (position - 1) / 2;
			if(item.value.compareTo(_heapBaseV[parentPosition]) > 0)
				return;

			swap(position, position = parentPosition);
		}
	}

	public KVPair<Integer,String> get()
	{
		if(_heapBaseV[0] == null)
			return null;

		KVPair<Integer,String> smallest = new KVPair<Integer, String>(_heapBaseK[0], _heapBaseV[0]);

		_heapBaseK[0] = _heapBaseK[--_size];
		_heapBaseV[0] = _heapBaseV[_size];


		downHeapFrom(0);
		return smallest;
	}

	public KVPair<Integer,String> replace(KVPair<Integer,String> item)
	{
		int smallestK = _heapBaseK[0];
		String smallestV = _heapBaseV[0];

		_heapBaseK[0] = item.key;
		_heapBaseV[0] = item.value;
		downHeapFrom(0);

		return new KVPair<Integer, String>(smallestK,smallestV);
	}

	public KVPair<Integer,String> peek()
	{
		return new KVPair<Integer, String>(_heapBaseK[0], _heapBaseV[0]);
	}
	public int size()
	{
		return _size;
	}
	public int capacity()
	{
		return _heapBaseK.length;
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

			if(d + 1 < _size && _heapBaseV[d].compareTo(_heapBaseV[d + 1]) > 0)
				d++;

			if(_heapBaseV[position].compareTo(_heapBaseV[d]) > 0)
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
		int tmpK = _heapBaseK[idxA];
		String tmpV = _heapBaseV[idxA];

		_heapBaseK[idxA] = _heapBaseK[idxB];
		_heapBaseV[idxA] = _heapBaseV[idxB];


		_heapBaseK[idxB] = tmpK;
		_heapBaseV[idxB] = tmpV;
	}


}
