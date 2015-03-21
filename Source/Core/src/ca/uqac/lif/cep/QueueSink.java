/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2015 Sylvain Hall�

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
package ca.uqac.lif.cep;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

/**
 * Sink that accumulates events into queues
 * @author sylvain
 *
 */
public class QueueSink extends Sink
{
	protected Vector<Queue<Object>> m_queues;
	
	public QueueSink(int in_arity)
	{
		super(in_arity);
		reset();
	}
	
	@Override
	public void reset()
	{
		super.reset();
		int arity = getInputArity();
		m_queues = new Vector<Queue<Object>>();
		for (int i = 0; i < arity; i++)
		{
			m_queues.add(new LinkedList<Object>());
		}

	}

	@Override
	protected Vector<Object> compute(Vector<Object> inputs)
	{
		for (int i = 0; i < m_queues.size(); i++)
		{
			Queue<Object> q = m_queues.get(i);
			q.add(inputs.get(i));
		}
		return new Vector<Object>();
	}
	
	public Queue<Object> getQueue(int i)
	{
		return m_queues.get(i);
	}
	
	/**
	 * Removes the first event of all queues
	 * @return A vector containing the first event of all queues, or null
	 */
	public Vector<Object> remove()
	{
		int num_queues = m_queues.size();
		Vector<Object> out = new Vector<Object>();
		for (int i = 0; i < num_queues; i++)
		{
			Queue<Object> q = m_queues.get(i);
			if (q.isEmpty())
			{
				out.add(null);
			}
			else
			{
				Object o = q.remove();
				out.add(o);				
			}
		}
		return out;
	}

}
