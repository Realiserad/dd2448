import java.math.BigInteger;
import java.util.Random;
public class Main {
    final int[] votes = { 0, 0, 1, 0, 0, 1, 1, 1, 1 };
    final BigInteger v = BigInteger.valueOf(votes.length + 1); // size of finite field
    final int n = 13; // security parameter
    final Random rng = new Random();
    private final class SealedVote {
        public final BigInteger p;
        public final BigInteger q;
        public final BigInteger encryptedVote;
        public SealedVote(BigInteger p, BigInteger q, BigInteger encryptedVote) {
            this.p = p;
            this.q = q;
            this.encryptedVote = encryptedVote;
        }
    }
    public static void main(String[] args) {
        new Main();
    }
    public Main() {
        final SealedVote[] sealedVotes = new SealedVote[votes.length];
        for (int i = 0; i < sealedVotes.length; i++)
            sealedVotes[i] = encrypt(votes[i], i);
        // Compute sum of encrypted votes
        BigInteger sum = BigInteger.ZERO;
        for (int i = 0; i < sealedVotes.length; i++)
            sum = sum.add(sealedVotes[i].encryptedVote);
        // Collect pq-pairs
        for (int i = 0; i < sealedVotes.length; i++) {
            for (int j = 0; j < sealedVotes.length; j++) {
                if (i != j)
                    sum = sum.add(sealedVotes[i].p.multiply(sealedVotes[j].q));
            }
        }
        // Compute joint key and compute election result
        BigInteger key = BigInteger.ZERO;
        for (int i = 0; i < sealedVotes.length; i++)
            key = key.add(sealedVotes[i].p);
        final int numberOfOnes = decrypt(key, sum);
        final int numberOfZeroes = votes.length - numberOfOnes;
        System.out.println("Number of zero votes:  " + numberOfZeroes);
        System.out.println("Number of one votes:   " + numberOfOnes);
        System.out.println("Total:                 " + votes.length);
        assert(votes.length == numberOfOnes + numberOfZeroes);
    }
    public SealedVote encrypt(final int m, int i) {
        BigInteger p = BigInteger.probablePrime(n, rng);
        BigInteger q = BigInteger.probablePrime(n, rng);
        BigInteger r = BigInteger.valueOf(2); // noise < p/2v
        BigInteger c = p.multiply(q).add(v.multiply(r)).add(BigInteger.valueOf(m));
        return new SealedVote(p, q, c);
    }
    public int decrypt(final BigInteger key, final BigInteger c) {
        return c.mod(key).mod(v).intValue();
    }
}
