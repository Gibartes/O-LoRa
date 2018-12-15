# Author : by Gibartes

import sqlite3,os,time
from collections import OrderedDict

# Create only one instance for a (handler) class

class Singleton(type):
    _instances = {}
    def __call__(cls, *args, **kwargs):
        if cls not in cls._instances:
            cls._instances[cls] = super(Singleton, cls).__call__(*args, **kwargs)
        return cls._instances[cls]


# You don't have to directly call this class instance on the outside.
# I'd recommend that create a handler class for the database querry.

class DataBaseQuerry:
    def __init__(self,dbname):
        self.TBL 	= dbname
        self.conn	= 0
        self.cursor = 0
    def __querry(self,sql):
        try:
            self.cursor.execute(sql)
            self.conn.commit()
            return True
        except:return False
    def __fetch(self,sql):
        try:
            self.cursor.execute(sql)
            for row in self.cursor:
                return row
        except:return False
    def connect(self,path):
        self.conn = sqlite3.connect(path)
        self.cursor = self.conn.cursor()
    def close(self):
        self.conn.close()
    def build(self,tables):
        self.conn.execute("""DROP TABLE IF EXISTS {0}""".format(self.TBL))
        self.conn.execute("""CREATE TABLE {0} ( {1}	)""".format(self.TBL,tables))
        self.conn.commit()
    def delete(self,ID):
        sql = "DELETE FROM {0} WHERE ID='{1}'".format(self.TBL,ID,ID)
        return self.__querry(sql)
    def insert(self,row):
        cols = ', '.join('"{}"'.format(col) for col in row.keys())
        vals = ', '.join(':{}'.format(col)  for col in row.keys())
        sql = 'INSERT INTO "{0}" ({1}) VALUES ({2})'.format(self.TBL,cols,vals)
        self.cursor.execute(sql,row)
        self.conn.commit()
    def read(self,ID):
        sql = "SELECT * FROM {0} WHERE ID='{1}' ORDER BY TMS DESC".format(self.TBL,ID)
        return self.__fetch(sql)
    def readByCol(self,ID,col):
        sql = "SELECT {0} FROM {1} WHERE ID='{2}' ORDER BY TMS DESC".format(col,self.TBL,ID)
        return self.__fetch(sql)    
    def modify(self,ID,col,data):
        sql = "UPDATE {0} SET {1}='{2}' WHERE ID='{3}'".format(self.TBL,col,data,ID)
        return self.__querry(sql)
    def getTop(self):
        sql = "SELECT * FROM {0} LIMIT 1".format(self.TBL)
        return self.__fetch(sql)
