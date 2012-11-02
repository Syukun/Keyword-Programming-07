import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Task04 {
	public List<String> getFruitList(){
		List<String> list = new ArrayList<String>();
		list.add("apple");
		list.add("grape");
		list.add("banana");

		return list;
	}


	public boolean isFruit(String food) {
	    Set<String> fruits = new HashSet<String>(getFruitList());
//	    return <<<fruits.contains(food)>>>;
//これはもとの評価値で成功する！
	    return fruits coontains food
	}
}
