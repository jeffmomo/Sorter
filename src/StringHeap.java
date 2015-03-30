/**
 * Heap implementation based on 0-based Array
 *
 *
 */
public class StringHeap
{
	private int _size = 0;
	private String[] _heapBase;

	// Creates an static sized, empty heap
	public StringHeap(int capacity)
	{
		_heapBase = new String[capacity];
	}

	// Heapifies an existing array, performing all necessary downheaps
	public StringHeap(String[] baseArray)
	{
		_heapBase = baseArray;

		// Gets the real size of the underlying array
		for(;_size < baseArray.length && baseArray[_size] != null; ++_size)


		for(int position = (_size - 1) / 2; position >= 0; position--)
		{
			downHeapFrom(position);
		}
	}

	// Inserts into the heap 1 item
	public void insert(String item)
	{
		_heapBase[_size++] = item;

		// Upheaps
		for(int position = _size - 1; position > 0;)
		{
			int parentPosition = (position - 1) / 2;
			if(item.compareTo(_heapBase[parentPosition]) > 0)
				return;

			// Swaps position of the parentPosition and position, and set position to parentPosition
			swap(position, position = parentPosition);
		}
	}

	// Removes the root of the heap, and returns it
	public String get()
	{
		if(_heapBase[0] == null)
			return null;

		String smallest = _heapBase[0];

		_heapBase[0] = _heapBase[--_size];

		downHeapFrom(0);
		return smallest;
	}

	// Replaces the root of the heap with another item, and returns the root.
	public String replace(String item)
	{
		String smallest = _heapBase[0];

		_heapBase[0] = item;
		downHeapFrom(0);

		return smallest;
	}

	// Places the item at the back of the array. Inaccessable by the actual heap.
	public void place(String item)
	{
		_heapBase[_size] = item;
	}

	// Returns the smallest element without removing it
	public String peek()
	{
		return _heapBase[0];
	}

	// Gets the size of the heap
	public int size()
	{
		return _size;
	}

	// Gets the heap's underlying array
	public String[] getBase()
	{
		return _heapBase;
	}

	// Gets the maximum size of the underlying array
	public int capacity()
	{
		return _heapBase.length;
	}


	// Downheaps from a position
	private void downHeapFrom(int fromPosition)
	{
		int position = fromPosition;

		// Loops while position does not exceed size
		for(;;)
		{
			int d = position * 2 + 1;
			if(d >= _size)
				return;

			if(d + 1 < _size && _heapBase[d].compareTo(_heapBase[d + 1]) > 0)
				d++;

			// If bigger than the biggest of the 2 children, then swap
			if(_heapBase[position].compareTo(_heapBase[d]) > 0)
			{
				swap(position, d);
				position = d;
			}
			else
			// Otherwise the downheap process is finished
				return;
		}
	}

	// Swaps the two elements of the array
	private void swap(int idxA, int idxB)
	{
		String tmp = _heapBase[idxA];

		_heapBase[idxA] = _heapBase[idxB];
		_heapBase[idxB] = tmp;
	}


}
