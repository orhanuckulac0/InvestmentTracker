package com.example.investmenttracker.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.investmenttracker.R
import com.example.investmenttracker.data.model.CoinModel
import com.example.investmenttracker.databinding.FragmentTokenDetailsBinding
import com.example.investmenttracker.domain.use_case.util.*
import com.example.investmenttracker.presentation.events.UiEvent
import com.example.investmenttracker.presentation.view_model.TokenDetailsViewModel
import com.example.investmenttracker.presentation.view_model.TokenDetailsViewModelFactory
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TokenDetailsFragment : Fragment() {

    private var binding: FragmentTokenDetailsBinding? = null
    @Inject
    lateinit var factory: TokenDetailsViewModelFactory
    lateinit var viewModel: TokenDetailsViewModel
    private var currentCoin: CoinModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_token_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTokenDetailsBinding.bind(view)

        viewModel = ViewModelProvider(this, factory)[TokenDetailsViewModel::class.java]
        setupActionBar()

        arguments?.let {
            currentCoin = it.getSerializable(Constants.PASSED_COIN) as CoinModel
        }

        setupView()

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_delete_coin, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId) {
                    R.id.actionDeleteCoin -> {
                        viewModel.deleteTokenFromDB(currentCoin!!)
                        findNavController().navigate(
                            R.id.action_tokenDetailsFragment_to_mainFragment
                        )
                        return true
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        lifecycleScope.launchWhenStarted {
            viewModel.eventFlow.collect {event->
                when(event) {
                    is UiEvent.ShowErrorSnackbar -> {
                        Snackbar.make(binding!!.root, event.message, Snackbar.LENGTH_LONG).show()
                    }else -> {}
                }
            }
        }

        // to prevent copy pasting
        binding!!.etTokenHeldAmount.setDecimalInput()
        binding!!.etTokenInvestmentAmount.setDecimalInput()

        binding!!.tokenDetailUpdateBtn.setOnClickListener {
            val totalTokenHeld = binding!!.etTokenHeldAmount.text.toString()
            val totalInvestment = binding!!.etTokenInvestmentAmount.text.toString()

            if (viewModel.checkEmptyInput(totalTokenHeld, totalInvestment)){
                val totalInvestmentWorth = formatTokenTotalValue(currentCoin!!.price, totalTokenHeld.toDouble()).replace(",", "").toDouble()
                viewModel.updateTokenDetails(currentCoin!!.cmcId, totalTokenHeld.toDouble(), totalInvestment.toDouble(), totalInvestmentWorth)
                findNavController().navigate(
                    R.id.action_tokenDetailsFragment_to_mainFragment
                )
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_tokenDetailsFragment_to_mainFragment)
            }
        })

    }

    @SuppressLint("SetTextI18n")
    private fun setupView(){
        val coinIcon = "https://s2.coinmarketcap.com/static/img/coins/64x64/"+"${currentCoin?.cmcId}"+".png"

        val regex = Regex("[^A-Za-z0-9 ]")

        binding!!.tvCoinName.text = currentCoin?.name + " / " + regex.replace(currentCoin!!.symbol, "")
        binding!!.tvTotalHeldAmount.text = formatTokenHeldAmount(currentCoin!!.totalTokenHeldAmount) + " " + regex.replace(currentCoin!!.symbol, "")
        binding!!.tvTotalInvestment.text = "$"+currentCoin?.totalInvestmentAmount.toString()
        binding!!.tvCurrentInvestmentValue.text =  "$"+currentCoin?.totalInvestmentWorth.toString()
        binding!!.tvProfitLossAmount.text = formatTotalProfitAmountUI(currentCoin!!)

        if (currentCoin?.totalInvestmentAmount == 0.0){
            binding!!.tvProfitLossAmount.setTextColor(requireContext().getColor(R.color.white))
            binding!!.tvProfitLossPercentage.setTextColor(requireContext().getColor(R.color.white))
            binding!!.tvProfitLossPercentage.text = "0.0%"
        }else{
            val profitLoss = (currentCoin!!.totalInvestmentWorth-currentCoin!!.totalInvestmentAmount).toString()
            if (profitLoss.contains("-")){
                binding!!.tvProfitLossAmount.setTextColor(requireContext().getColor(R.color.redColorPercentage))
                // TODO calculate profit / loss percentage and display on UI
            }else{
                binding!!.tvProfitLossAmount.setTextColor(requireContext().getColor(R.color.greenColorPercentage))
                // TODO calculate profit / loss percentage and display on UI
            }
        }


        Glide
            .with(this)
            .load(coinIcon)
            .thumbnail(Glide.with(this).load(R.drawable.spinner))
            .centerCrop()
            .placeholder(R.drawable.coin_place_holder)
            .into(binding!!.ivTokenDetailImage)
    }

    private fun setupActionBar(){
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding!!.toolbarTokenDetailsActivity)
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        if (actionBar != null){
            actionBar.title = "Token Details"
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.back_arrow_white)
            binding!!.toolbarTokenDetailsActivity.setNavigationOnClickListener {
                findNavController().navigate(R.id.action_tokenDetailsFragment_to_mainFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (currentCoin != null){
            currentCoin = null
        }
        if (binding != null){
            binding = null
        }
    }
}