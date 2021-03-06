package lighthouse.protocol;

import com.google.protobuf.*;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.*;
import org.bitcoinj.params.*;
import org.bitcoinj.script.*;
import org.javatuples.*;

public class TestUtils {
    private static byte[] counter = new byte[1];

    // Pledge for half the project value.
    public static Triplet<Transaction, Transaction, LHProtos.Pledge> makePledge(Project forProject, Address address, Coin projectValue) {
        final Coin pledgeVal = projectValue.divide(2);
        return makePledge(forProject, address, projectValue, pledgeVal);
    }

    public static Triplet<Transaction, Transaction, LHProtos.Pledge> makePledge(Project forProject, Address address, Coin projectValue, Coin pledgeVal) {
        ECKey key1 = new ECKey();
        Transaction tx0 = new Transaction(UnitTestParams.get());
        tx0.addInput(makeRandomInput());
        tx0.addOutput(pledgeVal, key1);
        Transaction tx1 = new Transaction(UnitTestParams.get());
        tx1.addOutput(projectValue, address);
        tx1.addSignedInput(tx0.getOutput(0), key1, Transaction.SigHash.ALL, true);
        LHProtos.Pledge.Builder pledge = LHProtos.Pledge.newBuilder();
        pledge.addTransactions(ByteString.copyFrom(tx1.bitcoinSerialize()));
        pledge.getPledgeDetailsBuilder().setTotalInputValue(pledgeVal.longValue());
        pledge.getPledgeDetailsBuilder().setProjectId(forProject.getID());
        pledge.getPledgeDetailsBuilder().setTimestamp(Utils.currentTimeSeconds());
        pledge.getPledgeDetailsBuilder();
        return new Triplet<>(tx0, tx1, pledge.build());
    }

    public static TransactionInput makeRandomInput() {
        TransactionSignature dummy = TransactionSignature.dummy();
        dummy = new TransactionSignature(dummy.toCanonicalised(), Transaction.SigHash.ALL, true);
        byte[] script = ScriptBuilder.createInputScript(dummy).getProgram();
        // Nonsense outpoint, it doesn't matter.
        counter[0]++;
        return new TransactionInput(UnitTestParams.get(), null, script, new TransactionOutPoint(UnitTestParams.get(), 0, Sha256Hash.hash(counter)));
    }
}
