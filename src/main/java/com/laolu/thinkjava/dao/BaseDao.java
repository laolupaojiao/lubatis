package com.laolu.thinkjava.dao;

import com.google.gson.annotations.Expose;
import org.apache.commons.dbcp2.BasicDataSource;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseDao {

    @Expose(serialize = false)
    private String query = "";
    @Expose(serialize = false)
    private boolean all = false;
    @Expose(serialize = false)
    private String where = "";
    @Expose(serialize = false)
    private String alins = "";
    @Expose(serialize = false)
    private String join = "";
    @Expose(serialize = false)
    private String field = " * ";
    @Expose(serialize = false)
    private String table = "";
    @Expose(serialize = false)
    private String value = "";
    @Expose(serialize = false)
    private String sets = "";
    @Expose(serialize = false)
    private String column = "(";
    @Expose(serialize = false)
    private String pk = "id";
    @Expose(serialize = false)
    private String pkv = "";
    @Expose(serialize = false)
    private String queryType = "";
    @Expose(serialize = false)
    private List<Object> values = new ArrayList<>();

    static BasicDataSource basicDataSource = new BasicDataSource();
    static Connection connection;

    static {
        basicDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        basicDataSource.setUrl("jdbc:mysql://localhost:3306/ship?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        basicDataSource.setUsername("root");
        basicDataSource.setPassword("520523");
        try {
            connection = basicDataSource.getConnection();
            connection.setAutoCommit(false);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public String getQuery() {
        if (queryType.equals("update")) {
            query = "UPDATE " + table + " SET " + sets  + where;
            return query;
        }
        if (queryType.equals("insert")) {
            query = "INSERT INTO " + table + " " + column + " VALUES (" + value;
            return query;
        }
        if (queryType.equals("find")) {
            query = "SELECT * FROM " + table + where;
            return query;
        }
        query = "SELECT" + field + "FROM " + table + alins + join + where;
        return query;
    }

    public BasicDataSource getDataSource() throws SQLException {
        return basicDataSource;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public BaseDao isNeedAll(boolean is) {
        this.all = is;
        return this;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public BaseDao() {

    }

    public BaseDao where(String[][] filter) {
        where = " WHERE ";
        for (String[] strings : filter) {
            where += strings[0] + strings[1] + "? and ";
            values.add(strings[2]);
        }
        where = where.substring(0, where.length() - 4);
        return this;
    }

    public BaseDao set(String[][] filter) {
        for (String[] strings : filter) {
            sets += strings[0] +"=?,";
            values.add(strings[1]);
        }
        sets = sets.substring(0, sets.length() - 1);
        return this;
    }

    public BaseDao alins(String name) {
        alins = " " + name + " ";
        return this;
    }

    public BaseDao join(String[] table) {
        join += " join ";
        join += table[0] + " on " + table[1];
        join = table[2] + join;
        return this;
    }

    public BaseDao field(String fields) {
        field = "";
        field = " " + fields + " ";
        return this;
    }

    public BaseDao select(String table_name) {
        table += table_name;
        return this;
    }

    public BaseDao update(String table_name) {
        queryType = "update";
        table += table_name;
        return this;
    }

    @SuppressWarnings(value = "unchecked")
    public <R, T extends List<R>> T find(String pkv) throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        table = this.getClass().getName();
        table = table.substring(table.lastIndexOf('.') + 1).toLowerCase();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                String varName = field.getName();
                if (varName.equals("pk")) {
                    pk = (String) field.get(this);
                }
                if (varName.equals("table")) {
                    table = (String) field.get(this);
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
        queryType = "find";
        where = " where " + pk+"="+"?";
        values.add(pkv);
        return (T) execQuery(this.getClass());
    }

    public boolean save() throws IntrospectionException, SQLException {
        table = this.getClass().getName();
        table = table.substring(table.lastIndexOf('.') + 1).toLowerCase();
        List<String> vlist = new ArrayList<>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                // 对于每个属性，获取属性名
                String varName = field.getName();
                if (varName.equals("table")) {
                    table = (String) field.get(this);
                }
                if (varName.equals("pk")) {
                    pk = (String) field.get(this);
                    continue;
                }
                if (varName.equals(pk)) {
                    continue;
                }
                column += varName + ",";
                // 修改访问控制权限
                // 获取在对象f中属性fields[i]对应的对象中的变量
                Object o = field.get(this);
                value += "?,";
                values.add(o);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
        column = column.substring(0, column.toString().lastIndexOf(',')) + ")";
        value = value.substring(0, value.lastIndexOf(',')) + ")";
        queryType = "insert";
        return execUpdate();
    }

    public boolean update() throws IntrospectionException, SQLException {
        table = this.getClass().getName();
        table = table.substring(table.lastIndexOf('.') + 1).toLowerCase();
        List<String> vlist = new ArrayList<>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                // 对于每个属性，获取属性名
                String varName = field.getName();
                if (varName.equals("table")) {
                    table = (String) field.get(this);
                }
                if (varName.equals("pk")) {
                    pk = (String) field.get(this);
                    continue;
                }
                if (varName.equals(pk)) {
                    pkv =  String.valueOf(field.get(this));
                    continue;
                }
                sets += varName + "=?,";
                // 修改访问控制权限
                // 获取在对象f中属性fields[i]对应的对象中的变量
                Object o = field.get(this);
                values.add(o);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
        where = " where " + pk + "=?";
        values.add(pkv);
        sets = sets.substring(0, sets.toString().lastIndexOf(','));
        queryType = "update";
        if (pkv == null || Integer.parseInt(pkv) == -1) {
            System.out.println("未设置主键值！");
        }
        return execUpdate();
    }

    public boolean execUpdate() {
        System.out.println(getQuery());
        System.out.println(getQuery());
        System.out.println(values.toString());
        PreparedStatement nstatement = null;
        try {
            nstatement = connection.prepareStatement(getQuery());
            for (int i = 0; i < values.size(); i++) {
                System.out.println(values.get(i));
                if (values.get(i) instanceof Integer) {
                    nstatement.setInt(i + 1, (Integer) values.get(i));
                    continue;
                }
                if (values.get(i) instanceof Float) {
                    nstatement.setFloat(i + 1, (Float) values.get(i));
                    continue;
                }
                nstatement.setString(i + 1, (String) values.get(i));
            }
            if (nstatement.executeUpdate() == 1) {
                connection.commit();
                return true;
            }
            return false;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public int execUpdateWithNoCommit() {
        System.out.println(getQuery());
        System.out.println(values.toString());
        PreparedStatement nstatement = null;
        try {
            nstatement = connection.prepareStatement(getQuery());
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i) instanceof Integer) {
                    nstatement.setInt(i + 1, (Integer) values.get(i));
                    continue;
                }
                if (values.get(i) instanceof Float) {
                    nstatement.setFloat(i + 1, (Float) values.get(i));
                    continue;
                }
                nstatement.setString(i + 1, String.valueOf(values.get(i)));
            }
            return nstatement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return 0;
        }
    }

    public <T> List<T> execQuery(Class<T> tClass) throws SQLException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        System.out.println(getQuery());
        System.out.println(values.toString());
        PreparedStatement statement = connection.prepareStatement(getQuery());
        List<T> result = new ArrayList<T>();
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) instanceof Integer) {
                statement.setInt(i + 1, (Integer) values.get(i));
                continue;
            }
            if (values.get(i) instanceof Float) {
                statement.setFloat(i + 1, (Float) values.get(i));
                continue;
            }
            statement.setString(i + 1, String.valueOf(values.get(i)));
        }
        ResultSet resultSet = statement.executeQuery();
        Field[] fields = tClass.getDeclaredFields();

        while (resultSet.next()) {
            T t = tClass.newInstance();
            if (all) {
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    String cname = resultSet.getMetaData().getColumnName(i);
                    String value = resultSet.getString(cname);
                    Field field = tClass.getDeclaredField(cname);
                    field.setAccessible(true);
                    if (field.getType() == int.class) {
                        field.set(t, Integer.parseInt(value));
                        continue;
                    }
                    if (field.getType() == float.class) {
                        field.set(t, Float.parseFloat(value));
                        continue;
                    }
                    field.set(t, value);
                }
            } else {
                for (Field value : fields) {
                    String varName = value.getName();
                    if (varName == "pk")
                        continue;
                    String cvalue = resultSet.getString(varName);
                    value.setAccessible(true);
                    if (value.getType() == int.class) {
                        value.set(t, Integer.parseInt(cvalue));
                        continue;
                    }
                    if (value.getType() == float.class) {
                        value.set(t, Float.parseFloat(cvalue));
                        continue;
                    }
                    if (value.getType() == String.class) {
                        value.set(t, cvalue);
                        continue;
                    }
                    value.set(t, value);
                }
            }
            result.add(t);
        }
        statement.close();
        return result;
    }

    public List<Map<String,String>> execQuery() throws SQLException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        System.out.println(getQuery());
        System.out.println(values.toString());
        PreparedStatement statement = connection.prepareStatement(getQuery());
        List<Map<String,String>> result = new ArrayList<Map<String,String>>();
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) instanceof Integer) {
                statement.setInt(i + 1, (Integer) values.get(i));
                continue;
            }
            if (values.get(i) instanceof Float) {
                statement.setFloat(i + 1, (Float) values.get(i));
                continue;
            }
            statement.setString(i + 1, String.valueOf(values.get(i)));
        }
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            //取出查询结果，并打印
            Map<String,String> item = new HashMap<String,String>();
            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                String cname = resultSet.getMetaData().getColumnName(i);
                String value = resultSet.getString(cname);
                item.put(cname,value);
            }
            result.add(item);
        }
        statement.close();
        return result;
    }

    public Boolean transaction(List<BaseDao> query_list) throws SQLException, InstantiationException, IllegalAccessException, NoSuchFieldException {

        List<Map<String,String>> result = new ArrayList<Map<String,String>>();
        boolean mark = true;
        try {
            for (BaseDao item:query_list) {
                PreparedStatement statement = connection.prepareStatement(item.getQuery());
                for (int i = 0; i < item.values.size(); i++) {
                    if (item.values.get(i) instanceof Integer) {
                        statement.setInt(i + 1, (Integer) item.values.get(i));
                        continue;
                    }
                    if (item.values.get(i) instanceof Float) {
                        statement.setFloat(i + 1, (Float) item.values.get(i));
                        continue;
                    }
                    statement.setString(i + 1, String.valueOf(item.values.get(i)));
                }
                if(statement.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
            }
            connection.commit();
            return true;
        } catch (Exception e) {
            connection.rollback();
            return false;
        }
    }
}