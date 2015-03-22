package org.comp317;



/**
 * Heap implementation based on 0-based Array
 *
 *
 */

// MinHeap
public class StringHeap
{
	private int _size = 0;
	private String[] _heapBase;

//
// Constructors
//
	public StringHeap(int capacity)
	{
		_heapBase = new String[capacity];
	}
	public StringHeap(String[] baseArray)
	{
		_heapBase = baseArray;
		_size = baseArray.length;

		for(int position = (_size - 1) / 2; position >= 0; position--)
		{
			downHeapFrom(position);
		}
	}

//
// Public Methods
//
	public void insert(String item)
	{
		_heapBase[_size++] = item;

		// Upheap
		for(int position = _size - 1; position > 0;)
		{
			int parentPosition = (position - 1) / 2;
			if(item.compareTo(_heapBase[parentPosition]) > 0)
				return;

			swap(position, position = parentPosition);
		}
	}

	public String get()
	{
		if(_heapBase[0] == null)
			return null;

		String smallest = _heapBase[0];

		_heapBase[0] = _heapBase[--_size];

		downHeapFrom(0);
		return smallest;
	}

	public String replace(String item)
	{
		String smallest = _heapBase[0];

		_heapBase[0] = item;
		downHeapFrom(0);

		return smallest;
	}
	public void place(String item)
	{
		_heapBase[_size] = item;
	}
	public String peek()
	{
		return _heapBase[0];
	}
	public int size()
	{
		return _size;
	}
	public String[] getBase()
	{
		return _heapBase;
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

			if(d + 1 < _size && _heapBase[d].compareTo(_heapBase[d + 1]) > 0)
				d++;

			if(_heapBase[position].compareTo(_heapBase[d]) > 0)
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
		String tmp = _heapBase[idxA];

		_heapBase[idxA] = _heapBase[idxB];
		_heapBase[idxB] = tmp;
	}


}
