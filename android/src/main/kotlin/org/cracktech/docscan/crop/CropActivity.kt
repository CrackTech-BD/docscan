package org.cracktech.docscan.crop
import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_crop.*
import org.cracktech.docscan.EdgeDetectionHandler
import org.cracktech.docscan.R
import org.cracktech.docscan.base.BaseActivity
import org.cracktech.docscan.view.PaperRectangle


class CropActivity : BaseActivity(), ICropView.Proxy {

    private lateinit var mPresenter: CropPresenter

    private lateinit var initialBundle: Bundle;

    private var  isGray:Boolean = false;

   // private lateinit var relativeLayout: RelativeLayout;

    override fun prepare() {
        this.initialBundle = intent.getBundleExtra(EdgeDetectionHandler.INITIAL_BUNDLE) as Bundle;
        this.title = initialBundle.getString(EdgeDetectionHandler.CROP_TITLE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //relativeLayout = findViewById(R.id.changeLayout)

       // getSupportActionBar().setDisplayHomeAsUpEnabled(true)


        paper.post {
            //we have to initialize everything in post when the view has been drawn and we have the actual height and width of the whole view
            mPresenter.onViewsReady(paper.width, paper.height)
        }
    }

    override fun provideContentViewId(): Int = R.layout.activity_crop


    override fun initPresenter() {
        val initialBundle = intent.getBundleExtra(EdgeDetectionHandler.INITIAL_BUNDLE) as Bundle;
        mPresenter = CropPresenter(this, this, initialBundle)
        findViewById<LinearLayout>(R.id.crop).setOnClickListener {
            mPresenter.crop()
            afterCrop()
        }

        findViewById<LinearLayout>(R.id.skip).setOnClickListener {
            mPresenter.skip()
            afterCrop()
        }




    }


    fun afterCrop(){

        findViewById<LinearLayout>(R.id.crop).visibility = View.GONE
        findViewById<LinearLayout>(R.id.skip).visibility = View.GONE

        findViewById<LinearLayout>(R.id.gray).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.rotate).visibility = View.VISIBLE






        findViewById<LinearLayout>(R.id.done).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.gray).setOnClickListener {

            if(isGray){
                isGray=false
                    findViewById<TextView>(R.id.gray_textview).setText("Scan Effect")
                mPresenter.reset()

            }else{
                isGray = true;
                findViewById<TextView>(R.id.gray_textview).setText("Undo Effect")

                mPresenter.enhance()
            }

        }

        findViewById<LinearLayout>(R.id.rotate).setOnClickListener {
            mPresenter.rotate()
        }


        findViewById<LinearLayout>(R.id.done).setOnClickListener {
            //item.setEnabled(false)
            //
            mPresenter.save()
            setResult(Activity.RESULT_OK)
            System.gc()
            finish()
        }
    }

    override fun getPaper(): ImageView = paper

    override fun getPaperRect(): PaperRectangle = paper_rect

    override fun getCroppedPaper(): ImageView = picture_cropped
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.crop_activity_menu, menu)
//
//        // menu.setGroupVisible(R.id.enhance_group, showMenuItems)
//
//        menu.findItem(R.id.rotation_image).isVisible = showMenuItems
//
//        menu.findItem(R.id.reset).isVisible = showMenuItems
//        menu.findItem(R.id.gray).isVisible = showMenuItems
//
//        menu.findItem(R.id.gray).title =
//            initialBundle.getString(EdgeDetectionHandler.CROP_BLACK_WHITE_TITLE) as String
//        menu.findItem(R.id.reset).title =
//            initialBundle.getString(EdgeDetectionHandler.CROP_RESET_TITLE) as String
//
//        if (showMenuItems) {
//            menu.findItem(R.id.action_label).isVisible = true
//            findViewById<ImageView>(R.id.crop).visibility = View.GONE
//        } else {
//            menu.findItem(R.id.action_label).isVisible = false
//            findViewById<ImageView>(R.id.crop).visibility = View.VISIBLE
//        }
//
//        return super.onCreateOptionsMenu(menu)
//    }
//
//
//    private fun changeMenuVisibility(showMenuItems: Boolean) {
//        this.showMenuItems = showMenuItems
//        invalidateOptionsMenu()
//    }
//
//    // handle button activities
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//
//        if (item.itemId == android.R.id.home) {
//            onBackPressed()
//            return true
//        }
//
//        if (item.itemId == R.id.action_label) {
//            Log.e(TAG, "Saved touched!")
//            // Bug fix: prevent clicking more than one time
//            item.setEnabled(false)
//            //
//            mPresenter.save()
//            setResult(Activity.RESULT_OK)
//            System.gc()
//            finish()
//            return true
//        } else if (item.itemId == R.id.rotation_image) {
//            Log.e(TAG, "Rotate touched!")
//            mPresenter.rotate()
//            return true
//        } else if (item.itemId == R.id.gray) {
//            Log.e(TAG, "Black White touched!")
//            mPresenter.enhance()
//            return true
//        } else if (item.itemId == R.id.reset) {
//            Log.e(TAG, "Reset touched!")
//            mPresenter.reset()
//            return true
//        }
//
//        return super.onOptionsItemSelected(item)    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
