package com.bitso;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.bitso.exchange.BookInfo;
import com.bitso.helpers.Helpers;

public class BitsoMockTest extends BitsoTest{
    private ArrayList<BookInfo> mockAvailableBooks;
    private BitsoTicker mockTicker;
    private BitsoOrderBook mockOrderBook;
    private BitsoAccountStatus mockAccountStatus;
    private BitsoBalance mockBalance;
    private BitsoFee mockFee;
    private BitsoOrder[] mockOpenOrders;
    private String mockOpenOrderId;
    private BitsoWithdrawal mockBtcWithdrawal;
    private BitsoWithdrawal mockEthWithdrawal;
    private BitsoWithdrawal mockSpeiWithdrawal;
    private Map<String, String> mockBitsoBanks;
    private BitsoWithdrawal mockDebitCardWithdrawal;
    private BitsoOperation[] mockLedgers;
    private BitsoWithdrawal[] mockWithdrawals;
    private BitsoFunding[] mockFundings;
    private BitsoTrade[] mockTrades;
    private BitsoOrder[] mockLookUpOrders;
    private String[] mockCanceledOrders;
    private Map<String, String> mockFundingDestination;

    @Before
    public void setUp() throws Exception{
        mBitso = Mockito.mock(Bitso.class);
        setUpTestMocks();
        setUpMockitoActions();
    }
    
    private void setUpTestMocks(){
        setUpAvailableBooks(Helpers.getJSONFromFile("publicAvailableBooks.json"));
        setUpTicker(Helpers.getJSONFromFile("publicTicker.json"));
        setUpOrderBook(Helpers.getJSONFromFile("publicOrderBook.json"));
        setUpAccountStatus(Helpers.getJSONFromFile("privateAccountStatus.json"));
        setUpAccountBalance(Helpers.getJSONFromFile("privateAccountBalance.json"));
        setUpFees(Helpers.getJSONFromFile("privateFees.json"));
        setUpLedgers(Helpers.getJSONFromFile("privateLedger.json"));
        setUpWithdrawals(Helpers.getJSONFromFile("privateWithdrawals.json"));
        setUpFundings(Helpers.getJSONFromFile("privateFundings.json"));
        setUpTrades(Helpers.getJSONFromFile("privateUserTrades.json"));
        setUpOpenOrders(Helpers.getJSONFromFile("privateOpenOrders.json"));
        setUpLookUpOrders(Helpers.getJSONFromFile("privateLookUpOrders.json"));
        setUpCanceledOrders(Helpers.getJSONFromFile("privateCancelOrder.json"));
        setUpPlaceOrder(Helpers.getJSONFromFile("privatePlaceOrder.json"));
        setUpFundingDestionation(Helpers.getJSONFromFile("privateFundingDestination.json"));
        setUpBtcWithdrawal(Helpers.getJSONFromFile("privateBitcoinWithdrawal.json"));
        setUpEthWithdrawal(Helpers.getJSONFromFile("privateEtherWithdrawal.json"));
        setUpSpeiWithdrawal(Helpers.getJSONFromFile("privateSPEIWithdrawal.json"));
        setUpBitsoBanks(Helpers.getJSONFromFile("privateBankCodes.json"));
        setUpDebitCardWithdrawal(Helpers.getJSONFromFile("privateDebitCardWithdrawal.json"));
    }

    private void setUpMockitoActions(){
        Mockito.when(mBitso.availableBooks()).thenReturn(mockAvailableBooks);
        Mockito.when(mBitso.getTicker(BitsoBook.BTC_MXN)).thenReturn(mockTicker);
        Mockito.when(mBitso.getOrderBook(BitsoBook.BTC_MXN)).thenReturn(mockOrderBook);
        Mockito.when(mBitso.getUserAccountStatus()).thenReturn(mockAccountStatus);
        Mockito.when(mBitso.getUserAccountBalance()).thenReturn(mockBalance);
        Mockito.when(mBitso.getUserFees()).thenReturn(mockFee);
        Mockito.when(mBitso.getOpenOrders()).thenReturn(mockOpenOrders);
        Mockito.when(mBitso.placeOrder(BitsoBook.BTC_MXN, BitsoOrder.SIDE.BUY,
                BitsoOrder.TYPE.LIMIT, new BigDecimal("15.4"), null,
                new BigDecimal("20854.4"))).thenReturn(mockOpenOrderId);
        Mockito.when(mBitso.bitcoinWithdrawal(new BigDecimal("0.001"),
                "31yTCKDHTqNXF5eZcsddJDe76BzBh8pVLb")).thenReturn(mockBtcWithdrawal);
        Mockito.when(mBitso.etherWithdrawal(new BigDecimal("0.001"),
                "0xc83adea9e8fea3797139942a5939b961f67abfb8")).thenReturn(mockEthWithdrawal);
        Mockito.when(mBitso.speiWithdrawal(new BigDecimal("50"),
                "name", "surname", "044180001059660729", "testing reference",
                "5706")).thenReturn(mockSpeiWithdrawal);
        Mockito.when(mBitso.getBanks()).thenReturn(mockBitsoBanks);
        Mockito.when(mBitso.debitCardWithdrawal(new BigDecimal("50"),
                "name test", "surname test", "5579209071039769", "40044")).
                thenReturn(mockDebitCardWithdrawal);
        Mockito.when(mBitso.getUserLedger(null)).thenReturn(mockLedgers);
        Mockito.when(mBitso.getUserWithdrawals()).thenReturn(mockWithdrawals);
        Mockito.when(mBitso.getUserFundings()).thenReturn(mockFundings);
        Mockito.when(mBitso.getUserTrades()).thenReturn(mockTrades);
        Mockito.when(mBitso.lookupOrders("kRrcjsp5n9og98qa")).thenReturn(mockLookUpOrders);
        Mockito.when(mBitso.cancelOrder("pj251R8m6im5lO82")).thenReturn(mockCanceledOrders);
        Mockito.when((mBitso.fundingDestination("btc"))).thenReturn(mockFundingDestination);
        Mockito.when((mBitso.fundingDestination("eth"))).thenReturn(mockFundingDestination);
        Mockito.when((mBitso.fundingDestination("mxn"))).thenReturn(mockFundingDestination);
    }

    private void setUpAvailableBooks(JSONObject o){
        mockAvailableBooks = new ArrayList<BookInfo>();
        JSONArray arr = o.getJSONArray("payload");
        for (int i = 0; i < arr.length(); i++) {
            mockAvailableBooks.add(new BookInfo(arr.getJSONObject(i)));
        }
    }

    private void setUpTicker(JSONObject o){
        mockTicker = new BitsoTicker(o.getJSONObject("payload"));
    }

    private void setUpOrderBook(JSONObject o){
        if(o.has("payload")){
            mockOrderBook = new BitsoOrderBook(o.getJSONObject("payload"));
        }
    }

    private void setUpAccountStatus(JSONObject o){
        if(o.has("payload")){
            JSONObject payload = o.getJSONObject("payload");
            mockAccountStatus = new BitsoAccountStatus(payload);
        }
    }

    private void setUpAccountBalance(JSONObject o){
        mockBalance = new BitsoBalance(o);
   }

    private void setUpFees(JSONObject o){
        mockFee = new BitsoFee(o);
    }

    private void setUpOpenOrders(JSONObject o){
        if(o.has("payload")){
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            mockOpenOrders = new BitsoOrder[totalElements];
            for(int i=0; i<totalElements; i++){
                mockOpenOrders[i] = new BitsoOrder(payload.getJSONObject(i));
            }
        }
    }

    private void setUpBtcWithdrawal(JSONObject o){
        if (o.has("payload")) {
            JSONObject payload = o.getJSONObject("payload");
            mockBtcWithdrawal = new BitsoWithdrawal(payload);
        }
    }

    private void setUpEthWithdrawal(JSONObject o){
        if (o.has("payload")) {
            JSONObject payload = o.getJSONObject("payload");
            mockEthWithdrawal = new BitsoWithdrawal(payload);
        }
    }

    private void setUpSpeiWithdrawal(JSONObject o){
        if (o.has("payload")) {
            JSONObject payload = o.getJSONObject("payload");
            mockSpeiWithdrawal = new BitsoWithdrawal(payload);
        }
    }

    private void setUpBitsoBanks(JSONObject o){
        if (o.has("payload")){
            mockBitsoBanks = new HashMap<String, String>();
            JSONArray payload = o.getJSONArray("payload");
            String currentBankCode = "";
            String currentBankName = "";
            JSONObject currentJSON = null;;
            int totalElements = payload.length();
            for(int i=0; i<totalElements; i++){
                currentJSON = payload.getJSONObject(i);
                currentBankCode = Helpers.getString(currentJSON, "code");
                currentBankName = Helpers.getString(currentJSON, "name");
                mockBitsoBanks.put(currentBankCode, currentBankName);
            }
        }
    }

    private void setUpDebitCardWithdrawal(JSONObject o){
        if (o.has("payload")) {
            JSONObject payload = o.getJSONObject("payload");
            mockDebitCardWithdrawal = new BitsoWithdrawal(payload);
        }
    }

    private void setUpLedgers(JSONObject o){
        if(o.has("payload")){
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            mockLedgers = new BitsoOperation[totalElements];
            for(int i=0; i<totalElements; i++){
                mockLedgers[i] = new BitsoOperation(payload.getJSONObject(i));
            }
        }
    }

    private void setUpWithdrawals(JSONObject o){
        if(o.has("payload")){
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            mockWithdrawals = new BitsoWithdrawal[totalElements];
            for(int i=0; i<totalElements; i++){
                mockWithdrawals[i] = new BitsoWithdrawal(payload.getJSONObject(i));
            }
        }
    }

    public void setUpFundings(JSONObject o){
        if(o.has("payload")){
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            mockFundings = new BitsoFunding[totalElements];
            for(int i=0; i<totalElements; i++){
                mockFundings[i] = new BitsoFunding(payload.getJSONObject(i));
            }
        }
    }

    public void setUpTrades(JSONObject o){
        if(o.has("payload")){
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            mockTrades = new BitsoTrade[totalElements];
            for(int i=0; i<totalElements; i++){
                mockTrades[i] = new BitsoTrade(payload.getJSONObject(i));
            }
        }
    }

    public void setUpLookUpOrders(JSONObject o){
        if(o.has("payload")){
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            mockLookUpOrders = new BitsoOrder[totalElements];
            for(int i=0; i<totalElements; i++){
                mockLookUpOrders[i] = new BitsoOrder(payload.getJSONObject(i));
            }
        }
    }

    public void setUpCanceledOrders(JSONObject o){
        mockCanceledOrders = Helpers.parseJSONArray(o.getJSONArray("payload"));
    }

    public void setUpPlaceOrder(JSONObject o){
        if (o.has("payload")) {
            JSONObject payload = o.getJSONObject("payload");
            mockOpenOrderId = Helpers.getString(payload, "oid");
        }
    }

    public void setUpFundingDestionation(JSONObject o){
        if (o.has("payload")) {
            JSONObject payload = o.getJSONObject("payload");
            mockFundingDestination =  new HashMap<String, String>();
            mockFundingDestination.put("accountIdentifierName",
                    Helpers.getString(payload, "account_identifier_name"));
            mockFundingDestination.put("accountIdentifier",
                    Helpers.getString(payload, "account_identifier"));
        }
    }
    
    @Test
    public void testSPEIWithdrawal(){
        BitsoWithdrawal speiWithdrawal =  mBitso.speiWithdrawal(new BigDecimal("50"),
                "name", "surname", "044180001059660729", "testing reference", "5706");
        assertEquals(true, nullCheck(speiWithdrawal, BitsoWithdrawal.class));
    }

    @Test
    public void testDebitCardWithdrawal(){
        BitsoWithdrawal debitCardWithdrawal = mBitso.debitCardWithdrawal(new BigDecimal("50"),
                "name test", "surname test", "5579209071039769", "40044");
        assertEquals(true, nullCheck(debitCardWithdrawal, BitsoWithdrawal.class));
    }
}
