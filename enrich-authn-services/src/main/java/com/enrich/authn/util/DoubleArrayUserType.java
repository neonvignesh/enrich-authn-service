package com.enrich.authn.util;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.*;

public class DoubleArrayUserType implements UserType {
    protected static final int  SQLTYPE = java.sql.Types.ARRAY;

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names, final SharedSessionContractImplementor sessionImplementor, final Object owner) throws HibernateException, SQLException {
        Array array = rs.getArray(names[0]);
        Double[] javaArray = (Double[]) array.getArray();
        return ArrayUtils.toPrimitive(javaArray);
    }

    @Override
    public void nullSafeSet(final PreparedStatement statement, final Object object, final int i, final SharedSessionContractImplementor sessionImplementor) throws HibernateException, SQLException {
        Connection connection = statement.getConnection();

        double[] castObject = (double[]) object;
        Double[] doubles = ArrayUtils.toObject(castObject);
        Array array = connection.createArrayOf("DOUBLE", doubles);

        statement.setArray(i, array);
    }

    @Override
    public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object deepCopy(final Object o) throws HibernateException {
        return o == null ? null : ((Double[]) o).clone();
    }

    @Override
    public Serializable disassemble(final Object o) throws HibernateException {
        return (Serializable) o;
    }

    @Override
    public boolean equals(final Object x, final Object y) throws HibernateException {
        return x == null ? y == null : x.equals(y);
    }

    @Override
    public int hashCode(final Object o) throws HibernateException {
        return o == null ? 0 : o.hashCode();
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
        return original;
    }

    @Override
    public Class<Double[]> returnedClass() {
        return Double[].class;
    }

    @Override
    public int[] sqlTypes() {
        return new int[] { SQLTYPE };
    }
}