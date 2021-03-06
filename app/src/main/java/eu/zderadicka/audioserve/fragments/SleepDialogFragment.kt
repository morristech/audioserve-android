package eu.zderadicka.audioserve.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.NumberPicker
import android.widget.Switch
import eu.zderadicka.audioserve.R
import eu.zderadicka.audioserve.utils.SLEEP_START_ACTION
import eu.zderadicka.audioserve.utils.SleepService


// All four must be dividable by 5
private const val MAX_SLEEP = 120
private const val DEFAULT_SLEEP = 30
private const val MAX_EXTEND = 120
private const val DEFAULT_EXTEND = 15

private fun calcSteps (max:Int): Array<String> {
    return (1..minsToVal(max)).map{ valToMins(it).toString()}.toTypedArray()
}

private fun valToMins(v:Int) = 5*v
private fun minsToVal(m:Int):Int = m/5

class SleepDialogFragment : DialogFragment() {
    @SuppressLint("ApplySharedPref")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = activity?.layoutInflater?.inflate(R.layout.fragment_sleep, null)!!
        val sleepAfterPicker = view.findViewById<NumberPicker>(R.id.sleepAfter)
        val extendByPicker = view.findViewById<NumberPicker>(R.id.extendBy)

        val sps = PreferenceManager.getDefaultSharedPreferences(activity)

        sleepAfterPicker.minValue=1
        sleepAfterPicker.maxValue= minsToVal(MAX_SLEEP)
        sleepAfterPicker.wrapSelectorWheel = false
        extendByPicker.minValue=1
        extendByPicker.maxValue= minsToVal(MAX_EXTEND)
        extendByPicker.wrapSelectorWheel = false
        sleepAfterPicker.displayedValues = calcSteps(MAX_SLEEP)
        extendByPicker.displayedValues = calcSteps(MAX_EXTEND)
        var currentSleep = sps.getInt("pref_sleep", -1)
        if (currentSleep < 0)  {
            currentSleep = DEFAULT_SLEEP
            sps.edit().putInt("pref_sleep", currentSleep).commit()
        }
        sleepAfterPicker.value = minsToVal(currentSleep)

        var currentExtend = sps.getInt("pref_extend", -1)
        if (currentExtend < 0 ) {
            currentExtend = DEFAULT_EXTEND
            sps.edit().putInt("pref_extend", currentExtend).commit()
        }
        extendByPicker.value = minsToVal(currentExtend)
        sleepAfterPicker.setOnValueChangedListener{_, _, newValue ->
            sps.edit().putInt("pref_sleep", valToMins(newValue)).apply()
        }
        extendByPicker.setOnValueChangedListener{_, _, newValue ->
            sps.edit().putInt("pref_extend", valToMins(newValue)).apply()
        }

        val soundState = sps.getBoolean("pref_sleep_notification_sound", false)
        val soundSwitch = view.findViewById<Switch>(R.id.soundSwitch)
        soundSwitch.isChecked = soundState
        soundSwitch.setOnCheckedChangeListener{
            _, isChecked ->
            sps.edit().putBoolean("pref_sleep_notification_sound", isChecked).apply()
        }

        val builder = AlertDialog.Builder(activity!!, R.style.SleepAlert)
                .setTitle(R.string.action_sleep_timer)
                .setIcon(R.drawable.ic_timer)
                .setView(view)
                .setPositiveButton("Start",  { _, _ ->

                    val intent = Intent(context, SleepService::class.java)
                    intent.action = SLEEP_START_ACTION
                    activity?.startService(intent)

                })
                .setNegativeButton("Cancel",  { _, _ ->

                })
        return builder.create()
    }
}