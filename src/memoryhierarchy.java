import java.util.*;

class Data{
    int index; // 최하위 메모리 내 주소값(데이터 호출, 식별시 사용)
    String word;
    int frequency;
}
class Block{
    int valid;
    int tag;
    Data data; // 데이터 담고 있음 == block
    long accessTime;
}
class Set1{
    int setIndex; // -> cache내 인덱스로 구성
    Block set[] = new Block[1];
}
class Set2{
    int setIndex; // -> cache내 인덱스로 구성
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

        ArrayList<Data> wordData = new ArrayList<>();

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
            wordData.add(new Data());
            wordData.get(i-1).index = i-1;
            wordData.get(i-1).word = tmp.get(i).get(1);
            wordData.get(i-1).frequency = Integer.parseInt(tmp.get(i).get(1));
        }

        //각 cache layer 생성
        Cache L1 = new Cache(1);
        Cache L2 = new Cache(16);
        Cache2way L3 = new Cache2way(256);
        Cache L4 = new Cache(4096);

        //cache에 set 채워주기 -> arraylist의 특성 때문
        L1.cache.add(new Set1());
        L1.cache.get(0).setIndex = 0;

        L2.cache.add(new Set1());
        for(int i = 0; i<L2.cacheSize; i++){
            L2.cache.add(new Set1());
            L2.cache.get(i).setIndex = i;
        }

        L3.cache2way.add(new Set2());
        for(int i = 0; i<L3.cacheSize; i++){
            L3.cache2way.add(new Set2());
            L3.cache2way.get(i).setIndex = i;
        }

        L4.cache.add(new Set1());
        for(int i = 0; i<L4.cacheSize; i++){
            L4.cache.add(new Set1());
            L4.cache.get(i).setIndex = i;
        }


        //시작 시간 확인
        long start = System.currentTimeMillis();


        // 데이터 호출 - 100,000번 기준
        for(int i = 0; i<101388; i++){

//          데이터 접근 -> 해당 부분 필요
            Data data  = new Data();

            //각 layer별 확인을 위한 플래그
            boolean flg = false;

            //L1에 있는지 없는지 확인
            //size가 0이 아닐 경우 캐시 탐색
            if(L1.cache.get(0).set[0].tag == data.index){
                L1_HitMissCount[0] ++;
                L1_accessTime += System.currentTimeMillis() - start;
                flg = true;
//                hitLayer = 1;
            }else{
                L1_HitMissCount[1]++;
                L1_accessTime += System.currentTimeMillis() - start;
            }

            // L2에 있는지 없는지 확인
            if (!flg){

                //size가 0이 아닐 경우 캐시 탐색
                for(int j = 0; j<L2.cache.size(); j++){

                    int L3setidx = (L2.cache.get(j).set[0].tag*256) + L2.cache.get(j).setIndex;
                    int L4setidx0 = (L3.cache2way.get(L3setidx).set[0].tag*256) + L3.cache2way.get(L3setidx).setIndex;
                    int memsetidx0 = (L4.cache.get(L4setidx0).set[0].tag*4096)+ L4.cache.get(L4setidx0).setIndex;
                    int L4setidx1 = (L3.cache2way.get(L3setidx).set[1].tag*256) + L3.cache2way.get(L3setidx).setIndex;
                    int memsetidx1 = (L4.cache.get(L4setidx1).set[0].tag*4096)+ L4.cache.get(L4setidx1).setIndex;

                    if(memsetidx0 == data.index && L2.cache.get(j).set[0].valid == 1){
                        L2_HitMissCount[0] ++;
                        L2_accessTime += System.currentTimeMillis() - start;
                        flg = true;

                        // hitLayer가 3인 경우 상위 캐시 레이어에 데이터 추가
                        int L2setIndex = L3setidx%16;

                        L1.cache.get(0).set[0].tag = L2setIndex/1;
                        L1.cache.get(0).set[0].valid = 1;
                        L1.cache.get(0).set[0].data = data;

                        break;
                    }else if(memsetidx1 == data.index && L2.cache.get(j).set[0].valid == 1){
                        L2_HitMissCount[0] ++;
                        L2_accessTime += System.currentTimeMillis() - start;
                        flg = true;

                        // hitLayer가 3인 경우 상위 캐시 레이어에 데이터 추가
                        int L2setIndex = L3setidx%16;

                        L1.cache.get(0).set[0].tag = L2setIndex/1;
                        L1.cache.get(0).set[0].valid = 1;
                        L1.cache.get(0).set[0].data = data;

                        break;
                    }
                }
                //L3에서 Miss -> 하단 코드로 넘어감, flg=false
                L2_HitMissCount[1]++;
                L2_accessTime += System.currentTimeMillis() - start;
            }

            // L3에 있는지 없는지 확인 + 각 block에 호출 timestamp 기록
            if (!flg){

                //size가 0이 아닐 경우 캐시 탐색
                for(int j = 0; j<L3.cacheSize; j++){
                    int L4setidx0 = (L3.cache2way.get(j).set[0].tag*256) + L3.cache2way.get(j).setIndex;
                    int memsetidx0 = (L4.cache.get(L4setidx0).set[0].tag*4096)+ L4.cache.get(L4setidx0).setIndex;
                    int L4setidx1 = (L3.cache2way.get(j).set[1].tag*256) + L3.cache2way.get(j).setIndex;
                    int memsetidx1 = (L4.cache.get(L4setidx1).set[0].tag*4096)+ L4.cache.get(L4setidx1).setIndex;

                    if(memsetidx0 == data.index && L3.cache2way.get(j).set[0].valid == 1){

                        L3_HitMissCount[0] ++;
                        L3.cache2way.get(j).set[0].accessTime = System.currentTimeMillis();
                        L3_accessTime += System.currentTimeMillis() - start;
                        flg = true;

                        // hitLayer가 3인 경우 상위 캐시 레이어에 데이터 추가
                        int L3setIndex = L3.cache2way.get(j).setIndex;

                        L2.cache.get(L3setIndex%16).set[0].tag = L3setIndex/16;
                        L2.cache.get(L3setIndex%16).set[0].valid = 1;
                        L2.cache.get(L3setIndex%16).set[0].data = data;
                        L2.cache.get(L3setIndex%16).setIndex = L3setIndex%16;
                        int L2setIndex = L3setIndex%16;

                        L1.cache.get(0).set[0].tag = L2setIndex/1;
                        L1.cache.get(0).set[0].valid = 1;
                        L1.cache.get(0).set[0].data = data;

                        break;

                    }else if(memsetidx1 == data.index && L3.cache2way.get(j).set[1].valid == 1){

                        L3_HitMissCount[0] ++;
                        L3.cache2way.get(j).set[1].accessTime = System.currentTimeMillis();
                        L3_accessTime += System.currentTimeMillis() - start;
                        flg = true;

                        // hitLayer가 3인 경우 상위 캐시 레이어에 데이터 추가
                        int L3setIndex = L3.cache2way.get(j).setIndex;

                        L2.cache.get(L3setIndex%16).set[0].tag = L3setIndex/16;
                        L2.cache.get(L3setIndex%16).set[0].valid = 1;
                        L2.cache.get(L3setIndex%16).set[0].data = data;
                        L2.cache.get(L3setIndex%16).setIndex = L3setIndex%16;
                        int L2setIndex = L3setIndex%16;

                        L1.cache.get(0).set[0].tag = L2setIndex/1;
                        L1.cache.get(0).set[0].valid = 1;
                        L1.cache.get(0).set[0].data = data;

                        break;
                    }
                }
                //L3에서 Miss -> 하단 코드로 넘어감, flg=false
                L3_HitMissCount[1]++;
                L3_accessTime += System.currentTimeMillis() - start;
            }

            // L4에 있는지 없는지 확인
            if (!flg){

                //캐시 탐색
                for(int j = 0; j<L4.cacheSize; j++){
                    //cpu에서 요구한 주소값과 cache의 주소값이 일치할 때, 해당 주소의 데이터를 cache에서 전달, flg = true
                    if( (((L4.cache.get(j).set[0].tag*4096)+ L4.cache.get(j).setIndex) == data.index) && (L4.cache.get(j).set[0].valid == 1) ){ // 수정 필요
                        L4_HitMissCount[0] ++;
                        L4_accessTime += System.currentTimeMillis() - start;
                        flg = true;

                        // hitLayer가 4인 경우 상위 캐시 레이어에 데이터 추가
                        int L4setIndex = L4.cache.get(j).setIndex;

                        if(L3.cache2way.get(L4setIndex%256).set.length < 2){
                            //해당 set에 자리가 남아있는 경우
                            L3.cache2way.get(L4setIndex%256).set[L3.cache2way.get(L4setIndex%256).set.length].tag = L4setIndex/256;
                            L3.cache2way.get(L4setIndex%256).set[L3.cache2way.get(L4setIndex%256).set.length].valid = 1;
                            L3.cache2way.get(L4setIndex%256).set[L3.cache2way.get(L4setIndex%256).set.length].data = data;
                        }else{
                            //자리가 없는 경우 LRU 고려
                            if(L3.cache2way.get(L4setIndex%256).set[0].accessTime > L3.cache2way.get(L4setIndex%256).set[1].accessTime){
                                L3.cache2way.get(L4setIndex%256).set[1].tag = L4setIndex/256;
                                L3.cache2way.get(L4setIndex%256).set[1].valid = 1;
                                L3.cache2way.get(L4setIndex%256).set[1].data = data;
                            }else{
                                L3.cache2way.get(L4setIndex%256).set[0].tag = L4setIndex/256;
                                L3.cache2way.get(L4setIndex%256).set[0].valid = 1;
                                L3.cache2way.get(L4setIndex%256).set[0].data = data;
                            }
                        }
                        int L3setIndex = L4setIndex%256;

                        L2.cache.get(L3setIndex%16).set[0].tag = L3setIndex/16;
                        L2.cache.get(L3setIndex%16).set[0].valid = 1;
                        L2.cache.get(L3setIndex%16).set[0].data = data;
                        L2.cache.get(L3setIndex%16).setIndex = L3setIndex%16;
                        int L2setIndex = L3setIndex%16;

                        L1.cache.get(0).set[0].tag = L2setIndex/1;
                        L1.cache.get(0).set[0].valid = 1;
                        L1.cache.get(0).set[0].data = data;

                        break;
                    }
                }
                //L4에서 Miss -> 하단 코드로 넘어감, flg=false
                L4_HitMissCount[1]++;
                L4_accessTime += System.currentTimeMillis() - start;
            }

            // hitLayer가 0인 경우 -> 캐시 어디에도 없음
            if(!flg){
                L4.cache.get(data.index%4096).set[0].tag = data.index/4096;
                L4.cache.get(data.index%4096).set[0].valid = 1;
                L4.cache.get(data.index%4096).set[0].data = data;
                L4.cache.get(data.index%4096).setIndex = data.index%4096;
                int L4setIndex = data.index%4096;

                if(L3.cache2way.get(L4setIndex%256).set.length < 2){
                    //해당 set에 자리가 남아있는 경우
                    L3.cache2way.get(L4setIndex%256).set[L3.cache2way.get(L4setIndex%256).set.length].tag = L4setIndex/256;
                    L3.cache2way.get(L4setIndex%256).set[L3.cache2way.get(L4setIndex%256).set.length].valid = 1;
                    L3.cache2way.get(L4setIndex%256).set[L3.cache2way.get(L4setIndex%256).set.length].data = data;
                }else{
                    //자리가 없는 경우 LRU 고려
                    if(L3.cache2way.get(L4setIndex%256).set[0].accessTime > L3.cache2way.get(L4setIndex%256).set[1].accessTime){
                        L3.cache2way.get(L4setIndex%256).set[1].tag = L4setIndex/256;
                        L3.cache2way.get(L4setIndex%256).set[1].valid = 1;
                        L3.cache2way.get(L4setIndex%256).set[1].data = data;
                    }else{
                        L3.cache2way.get(L4setIndex%256).set[0].tag = L4setIndex/256;
                        L3.cache2way.get(L4setIndex%256).set[0].valid = 1;
                        L3.cache2way.get(L4setIndex%256).set[0].data = data;
                    }

                }
                int L3setIndex = L4setIndex%256;

                L2.cache.get(L3setIndex%16).set[0].tag = L3setIndex/16;
                L2.cache.get(L3setIndex%16).set[0].valid = 1;
                L2.cache.get(L3setIndex%16).set[0].data = data;
                L2.cache.get(L3setIndex%16).setIndex = L3setIndex%16;
                int L2setIndex = L3setIndex%16;

                L1.cache.get(0).set[0].tag = L2setIndex/1;
                L1.cache.get(0).set[0].valid = 1;
                L1.cache.get(0).set[0].data = data;
//            L1.cache.get(L2setIndex%1).setIndex = 1;
            }
        }


        float L1_hitRatio = L1_HitMissCount[0]/101388;
        float L2_hitRatio = L2_HitMissCount[0]/101388;
        float L3_hitRatio = L3_HitMissCount[0]/101388;
        float L4_hitRatio = L4_HitMissCount[0]/101388;

        System.out.println("L1_hitRatio" + L1_hitRatio);
        System.out.println("L2_hitRatio" + L2_hitRatio);
        System.out.println("L3_hitRatio" + L3_hitRatio);
        System.out.println("L4_hitRatio" + L4_hitRatio);


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
