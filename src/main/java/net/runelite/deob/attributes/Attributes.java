package net.runelite.deob.attributes;

import net.runelite.deob.ClassFile;
import net.runelite.deob.Field;
import net.runelite.deob.Method;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class Attributes
{
	private ClassFile classFile;
	private Field field;
	private Method method;
	private Code code;

	private List<Attribute> attributes = new ArrayList<>();

	public Attributes(ClassFile cf, DataInputStream is) throws IOException
	{
		classFile = cf;

		load(is);
	}
	
	public Attributes(ClassFile cf)
	{
		classFile = cf;
	}

	public Attributes(Field f, DataInputStream is) throws IOException
	{
		field = f;

		load(is);
	}
	
	public Attributes(Field f)
	{
		field = f;
	}

	public Attributes(Method m)
	{
		method = m;
	}

	public Attributes(Code c)
	{
		code = c;
	}
	
	public Method getMethod()
	{
		return method;
	}

	public Attribute findType(AttributeType type)
	{
		for (Attribute a : attributes)
			if (a.getType() == type)
				return a;
		return null;
	}

	public ClassFile getClassFile()
	{
		if (classFile != null)
			return classFile;

		if (field != null)
			return field.getFields().getClassFile();

		if (method != null)
			return method.getMethods().getClassFile();

		if (code != null)
			return code.getAttributes().getClassFile();

		return null;
	}

	public void load(DataInputStream is) throws IOException
	{
		int count = is.readUnsignedShort();

		for (int i = 0; i < count; ++i)
		{
			String name = this.getClassFile().getPool().getUTF8(is.readUnsignedShort());

			AttributeType type = AttributeType.findType(name);
			try
			{
				Constructor<? extends Attribute> con = type.getAttributeClass().getConstructor(new Class[] { Attributes.class });
				Attribute attr = con.newInstance(this);
				attr.load(is);

				if (type != AttributeType.UNKNOWN)
					attributes.add(attr);
			}
			catch (Exception ex)
			{
				throw new IOException(ex);
			}
		}
	}
	
	public void write(DataOutputStream out) throws IOException
	{
		out.writeShort(attributes.size());
		for (Attribute a : attributes)
		{
			out.writeShort(this.getClassFile().getPool().makeUTF8(a.getType().getName()));
			a.write(out);
		}
	}
	
	public void addAttribute(Attribute a)
	{
		assert a.getAttributes() == this;
		attributes.add(a);
	}
}
