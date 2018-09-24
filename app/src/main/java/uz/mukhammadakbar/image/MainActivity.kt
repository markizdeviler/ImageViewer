package uz.mukhammadakbar.image

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initImageViewer()
    }

    private fun initImageViewer() {
        imageViewer.imageId(R.drawable.nature)
        imageViewer.imageUrl("http://apibazarway.wienerdeming.com/media/file/image/2018-08/948321c9-db06-4c78-a7e7-aba36e2208ef.jpg")
    }


}
