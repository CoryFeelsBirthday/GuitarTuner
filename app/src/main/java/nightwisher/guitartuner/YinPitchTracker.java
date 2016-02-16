package nightwisher.guitartuner;

public class YinPitchTracker {
    HistoryQueue<Float> freqHistory = new HistoryQueue<>(10);
    int sampleRate;
    float absoluteThresh = 0.05f;
    float maxAmp = 5000;
    float errorTolerance = 10;

    public YinPitchTracker(int sampleRate){
        this.sampleRate = sampleRate;
    }
    public float getFreq(){
        if(freqHistory.size()==0){
            return -1;
        }
        float sum=0;
        for(int i=0;i<freqHistory.size();i++){
            sum+=freqHistory.getItem(i);
        }
        return sum/freqHistory.size();
    }

    public void detectPitch(short[] data){
        int windowSize = data.length / 4;
        float[] d = new float[windowSize];
        float[] dPrime = new float[windowSize];
        int hoarseTau;
        for(int tau=0; tau<windowSize;tau++){
            d[tau] = 0;
            for(int j=1; j<windowSize;j++){
                float tmp = data[j] - data[j+tau];
                d[tau] += tmp * tmp;
            }
        }

        dPrime[0] = 1;
        for(int tau=1;tau<windowSize;tau++){
            float sum = 0;
            for(int j=1;j<tau;j++){
                sum += d[j];
            }
            dPrime[tau] = d[tau] * tau / sum;
        }

        int tau = 0;
        while(tau+1<windowSize&&(dPrime[tau+1]<=dPrime[tau]||dPrime[tau]>absoluteThresh)){
            tau++;
        }
        if(tau == windowSize-1){
            freqHistory.pop();
            return;
        }else{
            hoarseTau = tau;
        }


        int x0;
        int x1;
        int x2;
        if(hoarseTau==0){
            x0 = 0;
            x1 = 1;
            x2 = 2;
        }
        else if(hoarseTau==windowSize-1){
            x0 = windowSize - 3;
            x1 = windowSize - 2;
            x2 = windowSize - 1;
        }
        else{
            x0 = hoarseTau - 1;
            x1 = hoarseTau;
            x2 = hoarseTau + 1;
        }

        int x1x0 = x1 - x0;
        int x1x2 = x1 - x2;
        float y1y0 = dPrime[x1] - dPrime[x0];
        float y1y2 = dPrime[x1] - dPrime[x2];

        float bestTau = (float)(x1 - 0.5 * (x1x0 * x1x0 * y1y2 - x1x2 * x1x2 *y1y0) /
                (x1x0 * y1y2 - x1x2 * y1y0));

        float freq = sampleRate / bestTau;
        if(freqHistory.size()==0){
            freqHistory.push(freq);
        }else{
            float sum=0;
            for(int i=0;i<freqHistory.size();i++){
                sum+=freqHistory.getItem(i);
            }
            float error = Math.abs(freq - sum/freqHistory.size());
            if(error<errorTolerance){
                freqHistory.push(freq);
            }else{
                freqHistory.pop();
            }
        }
    }
}
