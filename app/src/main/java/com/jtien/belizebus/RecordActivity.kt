package com.jtien.belizebus

import android.app.ActionBar
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.*
import android.content.ClipData
import android.content.ClipboardManager
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.AttributeSet
import android.view.LayoutInflater


class RecordActivity : AppCompatActivity() {

    lateinit var rating_look: Rating
    lateinit var rating_know: Rating
    lateinit var rating_use: Rating
    lateinit var btn_submit: Button
    lateinit var edit_name: EditText
    lateinit var edit_commment: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record)

        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar?.setCustomView(R.layout.common_title)
        supportActionBar?.customView?.findViewById<TextView>(R.id.tvTitle)?.text = "FeedBack"
        supportActionBar?.setTitle("FeedBack")
        rating_look = findViewById<Rating>(R.id.record_rating_look)
        rating_know = findViewById<Rating>(R.id.record_rating_know)
        rating_use = findViewById<Rating>(R.id.record_rating_use)
        btn_submit = findViewById<Button>(R.id.record_submit)
        edit_name = findViewById<EditText>(R.id.record_name_content)
        edit_commment = findViewById<EditText>(R.id.record_comment_content)

        val preferences = getPreferences(Context.MODE_PRIVATE)
        var safeDeleteCnt = 0
        var safeDeleteNum = 10
        btn_submit.setOnClickListener {
            val score_look = rating_look.score
            val score_know = rating_know.score
            val score_use = rating_use.score
            rating_look.reset()
            rating_know.reset()
            rating_use.reset()
            val name = edit_name.text.toString()
            val comment = edit_commment.text.toString()
            edit_name.text.clear()
            edit_commment.text.clear()
            val str = "${score_look}^*^${score_know}^*^${score_use}^*^$name^*^$comment^**^\n"

            var allStr = preferences.getString("record", "")
            if(str != "0^*^0^*^0^*^^*^^**^\n"){
                allStr += str
                with (preferences.edit()) {
                    putString("record", allStr)
                    commit()
                }
                getRecordToClip(allStr)
                Toast.makeText(applicationContext,"Thanks for your Feedback!",Toast.LENGTH_SHORT).show()
                safeDeleteCnt=0
                finish()
            }else{
                getRecordToClip(allStr)

                if(safeDeleteCnt>safeDeleteNum){
                    safeDeleteCnt=0
                    with (preferences.edit()) {
                        putString("record", "")
                        commit()
                    }
                    Toast.makeText(applicationContext,"Record cleared",Toast.LENGTH_SHORT).show()
                }else{
                    safeDeleteCnt+=1
                }
            }
        }
    }

    fun getRecordToClip(str: String){
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clipData = ClipData.newPlainText("Source Text", str)
        clipboardManager?.primaryClip = clipData
    }

    object FeedReaderContract {
        // Table contents are grouped together in an anonymous object.
        object FeedEntry : BaseColumns {
            const val TABLE_NAME = "entry"
            const val COLUMN_NAME_TITLE = "title"
            const val COLUMN_NAME_SUBTITLE = "subtitle"
        }
        private const val SQL_CREATE_ENTRIES =
                "CREATE TABLE ${FeedEntry.TABLE_NAME} (" +
                        "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                        "${FeedEntry.COLUMN_NAME_TITLE} TEXT," +
                        "${FeedEntry.COLUMN_NAME_SUBTITLE} TEXT)"
        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${FeedEntry.TABLE_NAME}"
        class FeedReaderDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
            override fun onCreate(db: SQLiteDatabase) {
                db.execSQL(SQL_CREATE_ENTRIES)
            }
            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
                // This database is only a cache for online data, so its upgrade policy is
                // to simply to discard the data and start over
                db.execSQL(SQL_DELETE_ENTRIES)
                onCreate(db)
            }
            override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
                onUpgrade(db, oldVersion, newVersion)
            }
            companion object {
                // If you change the database schema, you must increment the database version.
                const val DATABASE_VERSION = 1
                const val DATABASE_NAME = "FeedReader.db"
            }
        }
    }
}

class Rating @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init{
        LayoutInflater.from(context).inflate(R.layout.record_rating, this, true)
        orientation = VERTICAL
        initBtns()
    }

    lateinit var btn_stars: MutableList<Button>
    var score = 0
    fun reset(){
        for (i in 0..4){btn_stars[i].text = "☆"}
        score = 0
    }
    fun initBtns(){
        btn_stars = mutableListOf()
        score = 0
        btn_stars.add(findViewById<Button>(R.id.record_start0))
        btn_stars.add(findViewById<Button>(R.id.record_start1))
        btn_stars.add(findViewById<Button>(R.id.record_start2))
        btn_stars.add(findViewById<Button>(R.id.record_start3))
        btn_stars.add(findViewById<Button>(R.id.record_start4))
        for(i in 0..4){
            btn_stars[i].setOnClickListener {
                for(j in 0..i){
                    this.btn_stars[j].text = "★"
                }
                for(j in (i+1)..4){
                    this.btn_stars[j].text = "☆"
                }
                score = i
            }
        }
    }
}