/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2016 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.cep.numbers;

import static org.junit.Assert.*;

import java.util.Queue;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.BeepBeepUnitTest;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.CumulativeProcessor;
import ca.uqac.lif.cep.functions.FunctionProcessor;
import ca.uqac.lif.cep.tmf.ConstantProcessor;
import ca.uqac.lif.cep.tmf.Filter;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.QueueSink;
import ca.uqac.lif.cep.tmf.QueueSource;
import ca.uqac.lif.cep.tmf.Window;

/**
 * Unit tests for basic arithmetic functions
 * @author Sylvain Hallé
 */
public class MathTest extends BeepBeepUnitTest
{

	@Before
	public void setUp() throws Exception
	{
	}
	
	@Test
	public void testSumPush() 
	{
		QueueSource cp = new QueueSource(1);
		cp.addEvent(1);
		Sum cs = new Sum();
		QueueSink qs = new QueueSink(1);
		Connector.connect(cp, cs);
		Connector.connect(cs, qs);
		Queue<Object> q = qs.getQueue(0);
		Number n;
		cp.push();
		n = (Number) q.remove();
		if (n.intValue() != 1)
		{
			fail("Wrong number");
		}
		cp.push();
		n = (Number) q.remove();
		if (n.intValue() != 2)
		{
			fail("Wrong number");
		}
		cp.push();
		n = (Number) q.remove();
		if (n.intValue() != 3)
		{
			fail("Wrong number");
		}
	}
	
	@Test
	public void testSumPull() 
	{
		QueueSource cp = new QueueSource(1);
		cp.addEvent(1);
		Sum cs = new Sum();
		QueueSink qs = new QueueSink(1);
		Connector.connect(cp, cs);
		Connector.connect(cs, qs);
		Queue<Object> q = qs.getQueue(0);
		Number n;
		qs.pull();
		n = (Number) q.remove();
		if (n.intValue() != 1)
		{
			fail("Wrong number");
		}
		qs.pull();
		n = (Number) q.remove();
		if (n.intValue() != 2)
		{
			fail("Wrong number");
		}
		qs.pull();
		n = (Number) q.remove();
		if (n.intValue() != 3)
		{
			fail("Wrong number");
		}
	}

	@Test
	public void testPower() 
	{
		Vector<Object> l_input1 = new Vector<Object>();
		l_input1.add(2);
		l_input1.add(3);
		l_input1.add(2);
		Vector<Object> l_input2 = new Vector<Object>();
		l_input2.add(4);
		l_input2.add(2);
		l_input2.add(0);
		QueueSource input1 = new QueueSource(1);
		input1.setEvents(l_input1);
		QueueSource input2 = new QueueSource(1);
		input2.setEvents(l_input2);
		FunctionProcessor pow = new FunctionProcessor(new Power());
		Connector.connect(input1, 0, pow, 0);
		Connector.connect(input2, 0, pow, 1);
		QueueSink sink = new QueueSink(1);
		Connector.connect(pow, sink);
		Number recv;
		input1.push();
		input2.push();
		recv = (Number) sink.remove()[0]; // 2^4
		if (recv == null || recv.intValue() != 16)
		{
			fail("Expected 16, got " + recv);
		}
		input1.push();
		input2.push();
		recv = (Number) sink.remove()[0]; // 3^2
		if (recv == null || recv.intValue() != 9)
		{
			fail("Expected 9, got " + recv);
		}
		input1.push();
		input2.push();
		recv = (Number) sink.remove()[0]; // 2^0
		if (recv == null || recv.intValue() != 1)
		{
			fail("Expected 1, got " + recv);
		}
	}
	
	@Test
	public void testStatisticalMomentPull1() 
	{
		Vector<Object> l_input1 = new Vector<Object>();
		l_input1.add(2);
		l_input1.add(3);
		l_input1.add(2);
		QueueSource input1 = new QueueSource(1);
		input1.setEvents(l_input1);
		QueueSink sink = new QueueSink(1);
		setupStatisticalMoment(input1, sink, 1);
		Number recv;
		sink.pull();
		recv = (Number) sink.remove()[0]; // 2
		if (recv == null || Math.abs(recv.floatValue() - 2) > 0.0001)
		{
			fail("Expected 2, got " + recv);
		}
		sink.pull();
		recv = (Number) sink.remove()[0]; // 2
		if (recv == null || Math.abs(recv.floatValue() - 2.5) > 0.0001)
		{
			fail("Expected 2.5, got " + recv);
		}
		sink.pull();
		recv = (Number) sink.remove()[0]; // 2
		if (recv == null || Math.abs(recv.floatValue() - 2.33333) > 0.0001)
		{
			fail("Expected 2.33, got " + recv);
		} 
	}
	
	@Test
	public void testStatisticalMomentPush1() 
	{
		Vector<Object> l_input1 = new Vector<Object>();
		l_input1.add(2);
		l_input1.add(3);
		l_input1.add(2);
		QueueSource input1 = new QueueSource(1);
		input1.setEvents(l_input1);
		QueueSink sink = new QueueSink(1);
		setupStatisticalMoment(input1, sink, 1);
		Number recv;
		input1.push();
		recv = (Number) sink.remove()[0]; // 2
		if (recv == null || Math.abs(recv.floatValue() - 2) > 0.0001)
		{
			fail("Expected 2, got " + recv);
		}
		input1.push();
		recv = (Number) sink.remove()[0]; // 2
		if (recv == null || Math.abs(recv.floatValue() - 2.5) > 0.0001)
		{
			fail("Expected 2.5, got " + recv);
		}
		input1.push();
		recv = (Number) sink.remove()[0]; // 2
		if (recv == null || Math.abs(recv.floatValue() - 2.33333) > 0.0001)
		{
			fail("Expected 2.33, got " + recv);
		} 
	}
	
	protected static void setupStatisticalMoment(Processor source, Processor sink, int n) 
	{
		Fork fork = new Fork(2);
		Connector.connect(source, fork);
		Sum sum_left = new Sum();
		{
			// Left part: sum of x^n
			Fork fork2 = new Fork(2);
			Connector.connect(fork, 0 ,fork2, 0);
			ConstantProcessor exponent = new ConstantProcessor(1);
			Connector.connect(fork2, 0, exponent, 0);
			FunctionProcessor pow = new FunctionProcessor(new Power());
			Connector.connect(fork2, 1, pow, 0);
			Connector.connect(exponent, 0, pow, 1);
			Connector.connect(pow, sum_left);
		}
		Sum sum_right = new Sum();
		{
			// Right part: sum of 1
			ConstantProcessor one = new ConstantProcessor(1);
			Connector.connect(fork, 1, one, 0);
			Connector.connect(one, sum_right);
		}
		FunctionProcessor div = new FunctionProcessor(Division.instance);
		Connector.connect(sum_left, 0, div, 0);
		Connector.connect(sum_right, 0, div, 1);
		Connector.connect(div, sink);
	}
	
	protected static void setupSumIfGreater(Processor source, Processor sink) 
	{
		Window win = new Window(new Sum(), 2);
		Connector.connect(source, win);
		Fork fork = new Fork(3);
		Connector.connect(win, fork);
		FunctionProcessor greater = new FunctionProcessor(IsGreaterThan.instance);
		ConstantProcessor five = new ConstantProcessor(5);
		Connector.connect(fork, 0, five, 0);
		Connector.connect(fork, 1, greater, 0);
		Connector.connect(five, 0, greater, 1);
		Filter fil = new Filter();
		Connector.connect(fork, 2, fil, 0);
		Connector.connect(greater, 0, fil, 1);
		Connector.connect(fil, sink);
	}

	@Test
	public void testSumIfGreaterPush() 
	{
		Vector<Object> l_input1 = new Vector<Object>();
		l_input1.add(2);
		l_input1.add(3);
		l_input1.add(4);
		l_input1.add(0);
		l_input1.add(6);
		QueueSource input1 = new QueueSource(1);
		input1.setEvents(l_input1);
		QueueSink sink = new QueueSink(1);
		setupSumIfGreater(input1, sink);
		Number recv;
		input1.push();
		input1.push();
		recv = (Number) sink.remove()[0]; // 2+3=5, null
		if (recv != null)
		{
			fail("Expected null, got " + recv);
		}
		input1.push();
		recv = (Number) sink.remove()[0]; // 3+4=7
		if (recv == null || recv.intValue() != 7)
		{
			fail("Expected 7, got " + recv);
		}
		input1.push();
		recv = (Number) sink.remove()[0]; // 4+0=4, null
		if (recv != null)
		{
			fail("Expected null, got " + recv);
		}
		input1.push();
		recv = (Number) sink.remove()[0]; // 0+6=6
		if (recv == null || recv.intValue() != 6)
		{
			fail("Expected 6, got " + recv);
		}
	}
	
	@Test
	public void testSumIfGreaterPullHard() 
	{
		Vector<Object> l_input1 = new Vector<Object>();
		l_input1.add(2);
		l_input1.add(3);
		l_input1.add(4);
		l_input1.add(0);
		l_input1.add(6);
		QueueSource input1 = new QueueSource(1);
		input1.setEvents(l_input1);
		QueueSink sink = new QueueSink(1);
		setupSumIfGreater(input1, sink);
		Number recv;
		sink.pullHard();
		recv = (Number) sink.remove()[0]; // 3+4=7
		assertEquals(7, recv.intValue());
		sink.pullHard();
		recv = (Number) sink.remove()[0]; // 0+6=6
		assertEquals(6, recv.intValue());
	}
	
	public static class Sum extends CumulativeProcessor
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -8722049367551551039L;

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Sum()
		{
			super(new CumulativeFunction(Addition.instance));
		}
	}

}
