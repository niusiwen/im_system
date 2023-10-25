package com.nsw.im.common.route.algorithm.consistenthash;

import com.nsw.im.common.enums.UserErrorCode;
import com.nsw.im.common.exception.ApplicationException;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author nsw
 * @date 2023/10/17 23:07
 */
public class TreeMapConsistentHash extends AbstractConsistentHash {

    private TreeMap<Long, String> treeMap = new TreeMap<>();

    // 为了防止节点只有两个并且值相差很大，造成获取其中一个的概率很大，
    // 这里增加虚拟节点来解决这个问题，虚拟节点的个数
    private static final int NODE_SIZE = 2;


    @Override
    protected void add(long key, String vlaue) {
        for(int i = 0; i < NODE_SIZE; i++){
            treeMap.put(super.hash("node"+ key + i), vlaue);
        }
        treeMap.put(key, vlaue);
    }

    @Override
    protected String getFirstNodeValue(String vlaue) {
        Long hash = super.hash(vlaue);
        // 返回指定键值对之后的所有键值对，如果没有大于或等于指定建的映射，则返回空的sortedMap
        SortedMap<Long, String> last = treeMap.tailMap(hash);
        if(!last.isEmpty()){
            return last.get(last.firstKey());
        }

        if(treeMap.size() ==0){
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        return treeMap.firstEntry().getValue();
    }

    @Override
    protected void processBefore() {
        // 清空节点，因为节点是动态的，为了防止节点下线之后，还能获取到，
        // 所以每次增加之前要先清空节点
        treeMap.clear();
    }
}
