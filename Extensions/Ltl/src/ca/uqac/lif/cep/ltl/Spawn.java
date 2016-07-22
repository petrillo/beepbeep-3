package ca.uqac.lif.cep.ltl;

import java.util.Collection;
import java.util.HashSet;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Connector.ConnectorException;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.SmartFork;
import ca.uqac.lif.cep.epl.NaryToArray;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.functions.FunctionProcessor;

public class Spawn extends Processor
{	
	/**
	 * The internal processor to evaluate the quantifier on
	 */
	protected Processor m_processor;

	/**
	 * The function to evaluate to create each instance of the quantifier.
	 * This function must return a <code>Collection</code> of objects;
	 * one instance of the internal processor will be created for each
	 * element of the collection.
	 */
	protected Function m_splitFunction;

	/**
	 * Each instance of the processor spawned by the evaluation of the
	 * quantifier
	 */
	protected Processor[] m_instances;
	
	/**
	 * The fork used to split the input to the multiple instances of the
	 * processor
	 */
	protected SmartFork m_fork;

	/**
	 * The passthrough synchronizing the output of each processor instance
	 */
	protected NaryToArray m_joinProcessor;

	/**
	 * The function processor used to combine the values of each
	 * instance 
	 */
	protected FunctionProcessor m_combineProcessor;
	
	/**
	 * The pushable that will detect when the first event comes
	 */
	protected SentinelPushable m_inputPushable;
	
	/**
	 * The pullable that will detect when the first event is requested
	 */
	protected SentinelPullable m_outputPullable;
	
	private Spawn()
	{
		super(1, 1);
	}

	public Spawn(Processor p, Function split_function, Function combine_function)
	{
		super(1, 1);
		m_processor = p;
		m_splitFunction = split_function;
		m_combineProcessor = new FunctionProcessor(combine_function);
		m_instances = null;
		m_fork = null;
		m_inputPushable = new SentinelPushable();
		m_outputPullable = new SentinelPullable();
	}
	
	@Override
	public Pushable getPushableInput(int index)
	{
		if (index != 0)
		{
			return null;
		}
		return m_inputPushable;
	}
	
	@Override
	public void setPullableInput(int index, Pullable p)
	{
		m_inputPushable.setPullable(p);
	}
	
	@Override
	public Pullable getPullableOutput(int index)
	{
		if (index != 0)
		{
			return null;
		}
		return m_outputPullable;
	}
	
	@Override
	public void setPushableOutput(int index, Pushable p)
	{
		//m_outputPullable.setPushable(p);
		m_outputPullable.setPushable(p);
	}
	
	protected class SentinelPushable implements Pushable
	{
		protected Pushable m_pushable = null;
		
		protected Pullable m_pullable = null;
		
		public SentinelPushable()
		{
			super();
		}
		
		public void setPushable(Pushable p)
		{
			m_pushable = p;
			if (m_fork != null && m_pullable != null)
			{
				m_fork.setPullableInput(0, m_pullable);
			}
		}

		@Override
		public Pushable push(Object o)
		{
			if (m_pushable == null)
			{
				spawn(o);
			}
			return m_pushable.push(o);
		}

		@Override
		public int getPushCount()
		{
			if (m_pushable == null)
			{
				return 0;
			}
			return m_pushable.getPushCount();
		}

		@Override
		public Processor getProcessor() 
		{
			return Spawn.this;
		}

		@Override
		public int getPosition() 
		{
			return 0;
		}
		
		public void setPullable(Pullable p)
		{
			m_pullable = p;
			if (m_fork != null)
			{
				m_fork.setPullableInput(0, m_pullable);
			}
		}
		
		public Pullable getPullable()
		{
			return m_pullable;
		}
	}
	
	protected class SentinelPullable implements Pullable
	{
		protected Pullable m_pullable = null;
		
		protected Pushable m_pushable = null;
		
		public SentinelPullable()
		{
			super();
		}
		
		@Override
		public Object pull()
		{
			if (m_pullable == null)
			{
				Object o = m_inputPushable.getPullable().pullHard();
				spawn(o);
			}
			return m_pullable.pull();
		}

		@Override
		public Object pullHard()
		{
			if (m_pullable == null)
			{
				Object o = m_inputPushable.getPullable().pullHard();
				spawn(o);
			}
			return m_pullable.pullHard();
		}

		@Override
		public NextStatus hasNext()
		{
			if (m_pullable == null)
			{
				Object o = m_inputPushable.getPullable().pullHard();
				spawn(o);
			}
			return m_pullable.hasNext();
		}

		@Override
		public NextStatus hasNextHard()
		{
			if (m_pullable == null)
			{
				Object o = m_inputPushable.getPullable().pullHard();
				spawn(o);
			}
			return m_pullable.hasNextHard();
		}

		@Override
		public int getPullCount() 
		{
			return m_pullable.getPullCount();
		}

		@Override
		public Processor getProcessor()
		{
			return Spawn.this;
		}

		@Override
		public int getPosition()
		{
			return 0;
		}
		
		public void setPullable(Pullable p)
		{
			m_pullable = p;
			if (m_combineProcessor != null && m_pushable != null)
			{
				m_combineProcessor.setPushableOutput(0, m_pushable);
			}
		}
		
		public void setPushable(Pushable p)
		{
			m_pushable = p;
			if (m_combineProcessor != null)
			{
				m_combineProcessor.setPushableOutput(0, m_pushable);
			}
		}
		
		public Pushable getPushable()
		{
			return m_pushable;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void spawn(Object o)
	{
		Object[] inputs = new Object[1];
		inputs[0] = o;
		Object[] function_value = m_splitFunction.evaluate(inputs, m_context);
		Collection<Object> values = null;
		if (function_value[0] instanceof Collection)
		{
			values = (Collection<Object>) function_value[0];
		}
		else
		{
			values = new HashSet<Object>();
			values.add(function_value[0]);
		}		
		try 
		{
			int size = values.size();
			m_fork = new SmartFork(values.size());
			m_inputPushable.setPushable(m_fork.getPushableInput(0));
			m_instances = new Processor[size];
			m_joinProcessor = new NaryToArray(size);
			int i = 0;
			for (Object slice : values)
			{
				Processor new_p = m_processor.clone();
				new_p.setContext(m_context);
				addContextFromSlice(new_p, slice);
				m_instances[i] = new_p;
				Connector.connect(m_fork, new_p, i, 0);
				Connector.connect(new_p, m_joinProcessor, 0, i);
				i++;
			}
			Connector.connect(m_joinProcessor, m_combineProcessor, 0, 0);
			m_outputPullable.setPullable(m_combineProcessor.getPullableOutput(0));
		}
		catch (ConnectorException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addContextFromSlice(Processor p, Object slice)
	{
		// Do nothing
	}
	
	@Override
	public Spawn clone()
	{
		Spawn out = new Spawn(m_processor.clone(), m_splitFunction.clone(), m_combineProcessor.getFunction().clone());
		return out;
	}
}
