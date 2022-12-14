package ro.pontes.dictfrro;

import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBAdapter2 {

	private final Context mContext;
	private SQLiteDatabase mDb;
	private DataBaseHelper2 mDbHelper;

	public DBAdapter2(Context context) {
		this.mContext = context;
		mDbHelper = new DataBaseHelper2(mContext);
	} // end constructor.

	public DBAdapter2 createDatabase() throws SQLException {
		try {
			mDbHelper.createDataBase();
		} catch (IOException mIOException) {
			throw new Error("UnableToCreateDatabase");
		}
		return this;
	}

	public DBAdapter2 open() throws SQLException {
		try {
			mDbHelper.openDataBase();
			mDbHelper.close();
			mDb = mDbHelper.getWritableDatabase();
			mDb.execSQL("PRAGMA foreign_keys=ON;");
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public Cursor queryData(String sql) {
		try {
			Cursor mCur = mDb.rawQuery(sql, null);
			if (mCur != null) {
				mCur.moveToNext();
			}
			return mCur;
		} catch (SQLException mSQLException) {
			// Log.e(TAG, "getTestData >>"+ mSQLException.toString());
			throw mSQLException;
		}
	}

	// A method to to execute SQL code:
	public boolean executeSQLCode(String sql) {
		mDb.execSQL(sql);
		return true;
	} // end executeSQLCode() method.

	// A method to update a table:
	public boolean updateData(String sql) {
		mDb.execSQL(sql);
		return true;
	} // end update data.

	// A method to insert into a table:
	public boolean insertData(String sql) {
		mDb.execSQL(sql);
		return true;
	} // end insert data.

	public boolean deleteData(String sql) {
		mDb.execSQL(sql);
		return true;
	} // end deleteData() method..

} // end DBAdapter2 class, for using my vocabulary.
