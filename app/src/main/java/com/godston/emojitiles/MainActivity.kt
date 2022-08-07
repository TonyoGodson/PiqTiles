package com.godston.emojitiles

import android.animation.ArgbEvaluator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.jinatonic.confetti.CommonConfetti
import com.godston.emojitiles.databinding.ActivityMainBinding
import com.godston.emojitiles.models.BoardSize
import com.godston.emojitiles.models.UserImageList
import com.godston.emojitiles.utils.EXTRA_BOARD_SIZE
import com.godston.emojitiles.utils.EXTRA_GAME_NAME
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val CREATE_REQUEST_CODE = 248
    }

    private lateinit var clRoot: CoordinatorLayout
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView

    private var db = Firebase.firestore
    private var gameName: String? = null
    private var customGameImages: List<String>? = null
    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdapter

    private var boardSize: BoardSize = BoardSize.EASY
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clRoot = binding.clRoot
        rvBoard = binding.rvBoard
        tvNumMoves = binding.tvNumMoves
        tvNumPairs = binding.tvNumPairs

        setUpBoard()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_refresh -> {
                if (memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()) {
                    showAlertDialog(
                        "Quit your current game?",
                        null,
                        View.OnClickListener {
                            setUpBoard()
                        }
                    )
                } else {
                    setUpBoard()
                }
                return true
            }
            R.id.ic_info -> {
                showInfoDialog()
            }
            R.id.mi_new_size -> {
                showNewSizeDialog()
                return true
            }
            R.id.mi_custom -> {
                showCreationDialog()
                return true
            }
            R.id.mi_download -> {
                showDownloadDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CREATE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val customGameName = data?.getStringExtra(EXTRA_GAME_NAME)
            if (customGameName == null) {
                Log.e(TAG, "Got null custom game from CreateActivity")
                return
            }
            downloadGame(customGameName)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showDownloadDialog() {
        val boardDownloadView = LayoutInflater.from(this).inflate(R.layout.dialog_download_board, null)
        showAlertDialog(
            "Fetch PiqTiles Game",
            boardDownloadView,
            View.OnClickListener {
//            Grab the text of the game name that the user wants to download
                val etDownloadGame = boardDownloadView.findViewById<EditText>(R.id.etDownloadGame)
                val gameToDownload = etDownloadGame.text.toString().trim()
                downloadGame(gameToDownload)
            }
        )
    }

    private fun downloadGame(customGameName: String) {
        db.collection("games").document(customGameName).get().addOnSuccessListener { document ->
            val userImageList = document.toObject(UserImageList::class.java)
            if (userImageList?.images == null) {
                Log.e(TAG, "Invalid custom game data from Firestore")
                Snackbar.make(clRoot, "Sorry, we couldn't find any such game, '$customGameName'", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(Color.parseColor("#FF0000"))
                    .setTextColor(Color.parseColor("#FFFFFF"))
                    .show()
                return@addOnSuccessListener
            }
            val numCards = userImageList.images.size * 2
            boardSize = BoardSize.getByValue(numCards)
            customGameImages = userImageList.images
            for (imageUrl in userImageList.images) {
                Picasso.get().load(imageUrl).fetch()
            }
            Snackbar.make(clRoot, "You are now playing '$customGameName'!", Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.parseColor("#FF0000"))
                .setTextColor(Color.parseColor("#FFFFFF"))
                .show()
            gameName = customGameName
            setUpBoard()
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Exception when retrieving game", exception)
        }
    }

    private fun showCreationDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

        showAlertDialog(
            "Create your own PiqTiles board",
            boardSizeView,
            View.OnClickListener {
                val desiredboardSize = when (radioGroupSize.checkedRadioButtonId) {
                    R.id.rbEasy -> BoardSize.EASY
                    R.id.rbMedium -> BoardSize.MEDIUM
                    else -> BoardSize.HARD
                }
                val intent = Intent(this, CreateActivity::class.java)
                intent.putExtra(EXTRA_BOARD_SIZE, desiredboardSize)
                startActivityForResult(intent, CREATE_REQUEST_CODE)
            }
        )
    }

    private fun showNewSizeDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        when (boardSize) {
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }
        showAlertDialog(
            "Choose game type",
            boardSizeView,
            View.OnClickListener {
                boardSize = when (radioGroupSize.checkedRadioButtonId) {
                    R.id.rbEasy -> BoardSize.EASY
                    R.id.rbMedium -> BoardSize.MEDIUM
                    else -> BoardSize.HARD
                }
                gameName = null
                customGameImages = null
                setUpBoard()
            }
        )
    }

    private fun showAlertDialog(title: String, view: View?, positiveClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("cancel", null)
            .setPositiveButton("OK") { _, _ ->
                positiveClickListener.onClick(null)
            }.show()
    }

    private fun showInfoDialog() {
        val logoutDialogLayout = LayoutInflater.from(this).inflate(R.layout.how_to_play, null)
        val alertBuilder = AlertDialog.Builder(this)
            .setView(logoutDialogLayout)
        val showDialog = alertBuilder.show()

        val howToPlayCloseBtn: Button? =
            logoutDialogLayout.findViewById(R.id.howToPlayCloseBtn)
        howToPlayCloseBtn?.setOnClickListener {
            showDialog.dismiss()
        }
    }

    private fun setUpBoard() {
        supportActionBar?.title = gameName ?: getString(R.string.app_name)
        when (boardSize) {
            BoardSize.EASY -> {
                tvNumMoves.text = "Easy: 4 x 2"
                tvNumPairs.text = "Pairs: 0 / 4"
            }
            BoardSize.MEDIUM -> {
                tvNumMoves.text = "Medium: 6 x 3"
                tvNumPairs.text = "Pairs: 0 / 9"
            }
            BoardSize.HARD -> {
                tvNumMoves.text = "Hard: 6 x 4"
                tvNumPairs.text = "Pairs: 0 / 12"
            }
        }
        tvNumPairs.setTextColor(ContextCompat.getColor(this, R.color.color_progress_none))
        memoryGame = MemoryGame(boardSize, customGameImages)
        adapter = MemoryBoardAdapter(
            this,
            boardSize,
            memoryGame.cards,
            object : MemoryBoardAdapter.CardClickListener {
                override fun onCardClicked(position: Int) {
                    updateGameWithFlip(position)
                }
            }
        )
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }
    private fun updateGameWithFlip(position: Int) {
        if (memoryGame.haveWonGame()) {
            Snackbar.make(clRoot, "You have already won", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(Color.parseColor("#FF0000"))
                .setTextColor(Color.parseColor("#FFFFFF"))
                .show()
            return
        }
        if (memoryGame.isCardFaceUp(position)) {
            Snackbar.make(clRoot, "Invalid move", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(Color.parseColor("#FF0000"))
                .setTextColor(Color.parseColor("#FFFFFF"))
                .show()
            return
        }
        if (memoryGame.flipCard(position)) {
            Log.i(TAG, "Found a match. ${memoryGame.numPairsFound} pairs found")
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                ContextCompat.getColor(this, R.color.color_progress_none),
                ContextCompat.getColor(this, R.color.color_progress_full)
            ) as Int
            tvNumPairs.setTextColor(color)
            tvNumPairs.text = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            if (memoryGame.haveWonGame()) {
                Snackbar.make(clRoot, "You won! Congratulations", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(Color.parseColor("#FF0000"))
                    .setTextColor(Color.parseColor("#FFFFFF"))
                    .show()
                CommonConfetti.rainingConfetti(clRoot, intArrayOf(Color.YELLOW, Color.GREEN, Color.RED, Color.MAGENTA)).oneShot()
            }
        }
        tvNumMoves.text = "Moves: ${memoryGame.getNumMoves()}"
        adapter.notifyDataSetChanged()
    }
}
