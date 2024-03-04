import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.integration1.R

object ActivityUtils {

    fun navigateToActivity(activity: Activity, intent: Intent) {
        activity.startActivity(intent)
    }

    fun showAboutDialog(activity: Activity) {
        val mView: View = activity.layoutInflater.inflate(R.layout.about, null)
        val tv = mView.findViewById<TextView>(R.id.app_version_id)
        val gmailIMG = mView.findViewById<ImageView>(R.id.gmail_img_id)
        val githubIMG = mView.findViewById<ImageView>(R.id.github_img_id)
        val instagramIMG = mView.findViewById<ImageView>(R.id.instagram_img_id)
        val linkedinIMG = mView.findViewById<ImageView>(R.id.linkedin_img_id)
        tv.text = activity.resources.getString(R.string.version)

        AlertDialog.Builder(activity)
            .setView(mView)
            .setCancelable(true)
            .show()

        gmailIMG.setOnClickListener {
            val uri = Uri.parse(activity.getString(R.string.email_info))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            activity.startActivity(intent)
        }

        githubIMG.setOnClickListener {
            val uri = Uri.parse(activity.getString(R.string.github_info))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            activity.startActivity(intent)
        }

        instagramIMG.setOnClickListener {
            val uri = Uri.parse(activity.getString(R.string.instagram_info))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            activity.startActivity(intent)
        }

        linkedinIMG.setOnClickListener {
            val uri = Uri.parse(activity.getString(R.string.linkedin_info))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            activity.startActivity(intent)
        }

    }

    fun relaunch(activity: Activity) {
        activity.finishAffinity()
        activity.startActivity(activity.intent)
    }


}