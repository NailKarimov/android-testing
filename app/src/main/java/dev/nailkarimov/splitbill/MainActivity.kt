package dev.nailkarimov.splitbill

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/** One screen: bill, tip, people → what each person pays. */
class MainActivity : AppCompatActivity() {
    private lateinit var bill: EditText
    private lateinit var tip: EditText
    private lateinit var people: EditText
    private lateinit var result: TextView
    private lateinit var error: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bill = findViewById(R.id.bill)
        tip = findViewById(R.id.tip)
        people = findViewById(R.id.people)
        result = findViewById(R.id.result)
        error = findViewById(R.id.error)

        findViewById<Button>(R.id.split).setOnClickListener { calculate() }
        findViewById<Button>(R.id.minus).setOnClickListener { step(-1) }
        findViewById<Button>(R.id.plus).setOnClickListener { step(+1) }
        people.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) calculate()
            false
        }

        savedInstanceState?.let {
            result.text = it.getString(KEY_RESULT, "")
            error.text = it.getString(KEY_ERROR, "")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_RESULT, result.text.toString())
        outState.putString(KEY_ERROR, error.text.toString())
    }

    private fun step(delta: Int) {
        val now = people.text.toString().trim().toIntOrNull() ?: 1
        people.setText((now + delta).coerceIn(1, BillSplitter.MAX_PEOPLE).toString())
    }

    private fun calculate() {
        BillSplitter.split(bill.text.toString(), tip.text.toString(), people.text.toString())
            .onSuccess { s ->
                error.text = ""
                result.text = getString(
                    R.string.result,
                    BillSplitter.format(s.perPersonCents),
                    BillSplitter.format(s.tipCents),
                    BillSplitter.format(s.totalCents),
                )
                if (s.extraCents > 0) {
                    result.append("\n" + getString(R.string.extra, BillSplitter.format(s.extraCents)))
                }
            }
            .onFailure { e ->
                result.text = ""
                error.text = (e as BillSplitter.Invalid).problem.message
            }
    }

    private companion object {
        const val KEY_RESULT = "result"
        const val KEY_ERROR = "error"
    }
}
