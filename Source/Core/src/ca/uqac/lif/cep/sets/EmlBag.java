/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2015 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.cep.sets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class EmlBag  
{
	protected Map<Object,Integer> m_map;
	
	public EmlBag()
	{
		super();
		m_map = new HashMap<Object, Integer>();
	}
	
	public EmlBag addAll(EmlBag b)
	{
		for (Object o : b.keySet())
		{
			if (!m_map.containsKey(o))
			{
				m_map.put(o, b.get(o));
			}
			else
			{
				int cardinality = m_map.get(o) + b.get(o);
				m_map.put(o, cardinality);
			}
		}
		return this;
	}
	
	/**
	 * Picks one element of the bag. This assumes you don't care
	 * about what element of the bag you get, as long as you get one.
	 * @return An element of the bag, or null if the bag is empty
	 */
	public Object getAnyElement()
	{
		Set<Object> objects = m_map.keySet();
		for (Object o : objects)
		{
			// Return the first element you pick
			return o;
		}
		return null;
	}
	
	public boolean contains(Object o)
	{
		if (!m_map.containsKey(o))
		{
			return false;
		}
		int cardinality = m_map.get(o);
		return cardinality > 0;
	}
	
	public EmlBag addElement(Object o)
	{
		if (!m_map.containsKey(o))
		{
			m_map.put(o, 1);
		}
		int cardinality = m_map.get(o);
		m_map.put(o,  cardinality + 1);
		return this;
	}
	
	public int get(Object o)
	{
		if (!m_map.containsKey(o))
		{
			return 0;
		}
		return m_map.get(o);
	}
	
	public Set<Object> keySet()
	{
		return m_map.keySet();
	}
	
	public EmlBag removeElement(Object o)
	{
		if (m_map.containsKey(o))
		{
			int cardinality = m_map.get(o);
			if (cardinality == 1)
			{
				m_map.remove(o);
			}
			else
			{
				m_map.put(o,  cardinality - 1);
			}
		}
		return this;
	}
	
	public int size()
	{
		int size = 0;
		for (int i : m_map.values())
		{
			size += i;
		}
		return size;
	}
	
	public EmlBag clear()
	{
		m_map.clear();
		return this;
	}
	
	public Iterator<Object> iterator()
	{
		return new BagIterator();
	}
	
	protected class BagIterator implements Iterator<Object>
	{
		protected Iterator<Object> m_keyIterator;
		
		protected Object m_currentKey;
		
		protected int m_currentKeyCount = 0;
		
		protected int m_currentKeyMax = 0;
		
		public BagIterator()
		{
			super();
			m_keyIterator = m_map.keySet().iterator();
			m_currentKey = null;
		}

		@Override
		public boolean hasNext()
		{
			if (m_currentKeyCount >= m_currentKeyMax)
			{
				if (!m_keyIterator.hasNext())
				{
					return false;
				}
				m_currentKey = m_keyIterator.next();
				m_currentKeyCount = 0;
				m_currentKeyMax = m_map.get(m_currentKey);				
			}
			return true;
		}

		@Override
		public Object next()
		{
			m_currentKeyCount++;
			return m_currentKey;
		}
		
	}

}