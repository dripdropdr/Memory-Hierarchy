import java.util.*;

class Data{
    int index; // 메모리 내 주소값
    String word; //
    int frequency;
}
class Block{
    int valid;
    int tag;
    Data data;
}
class Set1{
    // setIndex -> cache내 인덱스로 구성
    Block set[] = new Block[1];
}
class Set2{
    // setIndex -> cache내 인덱스로 구성
    Block set[] = new Block[2];
}

class Cache{
    int cacheSize;
    ArrayList<Set1> cache= new ArrayList<Set1>();

    public Cache(int cache_size){
        this.cacheSize = cache_size;
    }
}

class Cache2way{
    int cacheSize;
    ArrayList<Set2> cache2way= new ArrayList<Set2>();

    public Cache2way(int cache_size){
        this.cacheSize = cache_size;
    }
}

public class memoryhierarchy {

    public static void main(String[] args){

        CSVReader csvReader = new CSVReader();
        List<List<String>> tmp = csvReader.readCSV();

        ArrayList<Data> avocado = new ArrayList<>();

        //Hit Miss 기록
        int L1_HitMissCount[] = new int[2];
        int L2_HitMissCount[] = new int[2];
        int L3_HitMissCount[] = new int[2];
        int L4_HitMissCount[] = new int[2];

        // 한 레이어당 접근 시간 전체 더하는 변수,
        int L1_accessTime = 0;
        int L2_accessTime = 0;
        int L3_accessTime = 0;
        int L4_accessTime = 0;

        // 메모리 생성, 주소값 0 ~ 60383
        for(int i=1; i<tmp.size(); i++) {
            avocado.add(new Data());
            avocado.get(i-1).index = i-1;
            avocado.get(i-1).word = tmp.get(i).get(1);
            avocado.get(i-1).frequency = Integer.parseInt(tmp.get(i).get(1));
        }


        // 데이터 중 사용되는 빈도에 따른 등급 분할
//        int frequencyIndex[] = {20-1, 400-1, 3722-1, 14106-1};
        // 등급 별 데이터 호출 빈도
//        int frequencyCount[] = {77301, 20347, 2192, 110};
        //빈도


        //각 cache layer 생성
        Cache L1 = new Cache(1);
        Cache L2 = new Cache(16);
        Cache2way L3 = new Cache2way(256);
        Cache L4 = new Cache(4096);

        //시작 시간 확인
        long start = System.currentTimeMillis();

        // 랜덤 객체
//        Random rd = new Random();

        // 데이터 호출 - 100,000번 기준
        for(int i = 0; i<100000; i++){


//            //데이터 접근
//            int n;
//            while(true){
//                n = rd.nextInt(4);
//                if(frequencyCount[n] !=0){
//                    frequencyCount[n]--;
//                    break;
//                }
//            }
            Data data  = new Data();

            //각 layer별 확인을 위한 플래그
            boolean flg = false;
            int hitLayer = 0;

            //L1에 있는지 없는지 확인
            //cache size가 0일 경우
            if(L1.cache.size() == 0) {
                L1_HitMissCount[1]++;
                L1_accessTime += System.currentTimeMillis() - start;
            //size가 0이 아닐 경우 캐시 탐색
            }else if(L1.cache.get(0).set[0].tag == data.index){
                L1_HitMissCount[0] ++;
                L1_accessTime += System.currentTimeMillis() - start;
                flg = true;
                hitLayer = 1;
            }else{
                L1_HitMissCount[1]++;
                L1_accessTime += System.currentTimeMillis() - start;
            }

            // L2에 있는지 없는지 확인
            if (!flg){
                //cache size가 0일 경우
                if(L2.cache.size() == 0){
                    L2_HitMissCount[1]++;
                }
                //size가 0이 아닐 경우 캐시 탐색
                for(int j = 0; j<L2.cache.size(); j++){
                    if(L2.cache.get(j).set[0].tag == data.index){
                        L2_HitMissCount[0] ++;
                        flg = true;
                        hitLayer = 2;
                    }else{
                        L2_HitMissCount[1]++;
                    }
                }
            }

            // L3에 있는지 없는지 확인
            if (!flg){
                //cache size가 0일 경우
                if(L3.cache2way.size() == 0){
                    L3_HitMissCount[1]++;
                }
                //size가 0이 아닐 경우 캐시 탐색
                for(int j = 0; j<L3.cache2way.size(); j++){
                    if(L3.cache2way.get(j).set[0].tag == data.index || L3.cache2way.get(j).set[1].tag == data.index){
                        L3_HitMissCount[0] ++;
                        flg = true;
                        hitLayer = 3;
                    }else{
                        L3_HitMissCount[1]++;
                    }
                }
            }

            // L4에 있는지 없는지 확인
            if (!flg){
                //cache size가 0일 경우
                if(L4.cache.size() == 0){
                    L4_HitMissCount[1]++;
                }
                //size가 0이 아닐 경우 캐시 탐색
                for(int j = 0; j<L4.cache.size(); j++){
                    if(L4.cache.get(j).set[0].tag == data.index){
                        L4_HitMissCount[0] ++;
                        flg = true;
                        hitLayer = 4;
                    }else{
                        L4_HitMissCount[1]++;
                    }
                }
            }



        }



//        for(int i=0; i<avocado.size(); i++) {
//            System.out.print(avocado.get(i).key + "\n");
//        }
//        for(int i=0; i<tmp.size(); i++) {
//            for(int j=0; j<tmp.get(i).size(); j++) {
//                System.out.print("\t" + tmp.get(i).get(j));
//            }
//            System.out.println();
//        }
    }
}
