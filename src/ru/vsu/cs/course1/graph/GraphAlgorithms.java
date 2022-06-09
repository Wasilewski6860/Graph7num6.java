package ru.vsu.cs.course1.graph;

import java.util.*;
import java.util.function.Consumer;

public class GraphAlgorithms {

    /**
     * Поиск в глубину, реализованный рекурсивно
     * (начальная вершина также включена)
     * @param graph граф
     * @param from Вершина, с которой начинается поиск
     * @param visitor Посетитель
     */
    public static void dfsRecursion(Graph graph, int from, Consumer<Integer> visitor) {
        boolean[] visited = new boolean[graph.vertexCount()];

        class Inner {
            void visit(Integer curr) {
                visitor.accept(curr);
                visited[curr] = true;
                for (Integer v : graph.adjacencies(curr)) {
                    if (!visited[v]) {
                        visit(v);
                    }
                }
            }
        }
        new Inner().visit(from);

    }

    public static void dfs(Graph graph, List<List<Integer>> result, List<Integer> path, int start, int finish, boolean[] visited) {
        visited[start] = true;
        path.add(start);

        if (start == finish) {
            result.add(path);
        }
        else {

            List<Integer> tempPath = new ArrayList<>(path);
            boolean[] tempVisited = visited.clone();

            for (int v : graph.adjacencies(start)) {

                if (!visited[v]) {
                    dfs(graph, result, path, v, finish, visited);

                    path = new ArrayList<>(tempPath);
                    visited = tempVisited.clone();

                }
            }
        }
    }

    public static Graph listOfWaysToGraph(List<List<Integer>> listOfWays){
        Graph graph = new AdjMatrixDigraph();
        for (int i=0;i<listOfWays.size();i++){
            for (int j=0;j<listOfWays.get(i).size()-1;j++){
                if (!graph.isAdj(listOfWays.get(i).get(j),listOfWays.get(i).get(j+1)))
                graph.addAdge(listOfWays.get(i).get(j),listOfWays.get(i).get(j+1));
            }
        }
        return graph;
    }


    /**
        Метод по пропускаю тока через граф, т.е. мы принимаем некоторую вершину from за источник тока(причем неважно, реальный ли это источник тока или нет),
        и начинаем двигаться во все стороны, превращая неориентированные ребра в ориентированные,
        что в дальнейшем позволит с помощью алгоритма поиска всех путей найти тот подграф, на котором мы будем искать сопротивление всей цепи

         Пока что граф задается в виде матрицы инцидентности/смежности, с кодом Соломы совладать не могу

         @param from - начало рассматриваемого участка
         @param to  - конец рассматриваемого участка

     Метод работает корректно(вроде, проверял несколько графов, пока проблемных случаев не выявлено), в исправлении не нуждается
     */
    public static boolean[][] bfsMy(boolean[][] graph, int from,int to) {

        //  boolean[] visited = new boolean[graph.length];
        Queue<Integer> queue = new LinkedList<Integer>();
        queue.add(from);
        //     visited[from] = true;

        //Заводим новый граф
        boolean[][] newVisited = new boolean[graph.length][graph.length];
        //Пока что граф несвязный, путей между вершинами нет
        for (boolean[] lArr : newVisited){
            for (boolean b : lArr){
                b=false;
            }
        }

        while (queue.size() > 0) {

            Integer curr = queue.remove();
            //Проходимся по всем вершинам, смежным с текущей
            for(int i=0;i<graph[curr].length;i++){
               //Если в новом графе еще нет пути из curr  в i и наоборот, а так же в исходном, неориентированном графе такой путь вообще есть,
                // Так же рассматривается вариант, если мы уже выходили из вершины to в текущую,
                // в таком случае возможно создание двустороннего ребра, которое после заменится на одно ребро в вершину to
                if ((!newVisited[curr][i] && !newVisited[i][curr] && graph[curr][i]) ||(i==to) &&!newVisited[curr][i]&& graph[curr][i]) {
                    queue.add(i);
                    newVisited[curr][i]=true;
                }
            }

        }

        //Исправление случая с двусторонним ребром в/из конечной вершины, заменяем на односторонее ребро
        for (int i=0;i< newVisited[to].length;i++){
            if (newVisited[i][to]) newVisited[to][i]=false;
        }

        return newVisited ;
    }

    /**
     Поиск самого вложенного параллельного соединения
     Наша задача - найти простое параллельное соединение, т.е. не содержащее в себе прочих параллельных соединений
     Получаем на вход матрицу смежности/инцидентности, текущую вершину, откуда начинаем двигаться,
     текущий узел, после которого начинается простое параллельное соединение, и массив посещенных вершин
     */
    public static void searchingForTheMostNestedParallelConnection(boolean[][] adjMatrix,int curr,int k,boolean[] visited){
        //Заводим счетчик под число вершин, смежных с текущей
        int count =0;
        for (int i=0;i<adjMatrix[curr].length;i++){
            //Если вершина смежна и не была посещена, наращиваем счетчик
            if(adjMatrix[curr][i] && !visited[i]) count++;
        }
        //Повторно проходимся по всем смежным с текущей вершиной
        for (int i=0;i<adjMatrix[curr].length;i++){
            //Если путь есть, и вершина не посещена
            if(adjMatrix[curr][i] && !visited[i] ) {
                //Если текущая вершина является началом параллельного соединения, запоминаем его индекс в k
                if (count>1) k=curr;
                //Посещаем вершину
                visited[curr]=true;
                //И рекурсивно вызываем функцию из k
                searchingForTheMostNestedParallelConnection(adjMatrix, i, k, visited);
            }
        }

    }

    /**
     * Упрощение параллельного соединения
     * @param adjMatrix - матрица смежности
     * @param resistors - сопротивления резисторов
     * @param curr - текущая вершина, начало параллельного соединения
     */
    public static void simplifyParallel(boolean[][] adjMatrix,double[][] resistors,int curr){

        int Final=0;
        int chislitel=1;
        int znamenatel =1;
        double tempR=0;

        //Будем искать вершину, которая будет концом параллельного соединения,т.е. в которую будут сходиться прочие ребра параллельного соединения
        boolean didiWeFoundAnEnd = false;
        for (int i=0;i< adjMatrix[curr].length;i++){
            //Ищем конец пар-го соединения
            if (!didiWeFoundAnEnd) Final=searchForEndOfSerial(adjMatrix,i);
            //Для каждой смежной вершины упрощаем цепочку последовательных соединений, начало коей лежит в i, в один резистор
            if (adjMatrix[curr][i]){
              tempR=  simplifySerial(adjMatrix,resistors,i,0);
            }
            //Высчитываем собственно сопртивление паралельного участка
            chislitel*=tempR;
            znamenatel+=tempR;
            //Превращаем разветвление, начало которого лежит в [curr], в одиночный резистор, т.е. все боковые ответвления, которые мы только что учли, обрезаем
            //Тогда дальнейший поиск параллельных соединений не сочтет это соединение параллельным, но и не пропустит все вершины, лежащие после этого параллельного соединения
            //(Об этом чуть ниже)
            adjMatrix[curr][i]=false;

        }
        //Когда сопротивление посчитано(почти), записываем его в значение нового резистора
        //Для этого мы "стягиваем" текущую вершину, являющуюся началом разветвления, и конец разветвления
        resistors[curr][Final]=chislitel/znamenatel;
        //А так же заносим в матрицу смежности, что между этими вершинами есть ребро
        adjMatrix[curr][Final]=true;
    }

    /**Проверка на то, является ли граф просто цепочкой последовательных резисторов(когда остается лишь сложить все сопртивления)
     *
     * @param adjMatrix - матрица смежности графа
     * @return
     */
    public static boolean checkForSimplicity(boolean[][] adjMatrix){
        //Проходимся двойным циклом по всему графу
        for (int i=0;i<adjMatrix.length;i++){
            //Заводим счетчик смежных с вершиной i вершин
            int count=0;
            for (int j=0;j< adjMatrix[i].length;j++){
                //Если между i-й и j-й вершинами есть путь, наращиваем счетчик
                if (adjMatrix[i][j]) count++;
            }
            //Если значение счетчика больше одного, т.е. есть разветвления, граф не является окончательно упрощенным,
            //И другие методы должны будут упрощать его в дальнейшем
            if (count>1) return false;
        }
        return true;
    }

    /**
     * Конечный метод, возвращающий сопротивление цепи, начиная с узла curr
     * @param adjMatrix - матрица смежности
     * @param resistors - резисторы
     * @param curr - начальный узел
     * @return
     */

    /**
     * Упрощение последовательного соединения
     * @param adjMatrix - матрица смежности
     * @param resistors - значения резисторов
     * @param curr - текущая вершина
     * @param R - сопротивление
     * @return
     *
     * Проблемный метод, хотя по логике не сложный
     *
     */
    public static double simplifySerial(boolean[][] adjMatrix,double[][] resistors,int curr,double R){

        //Начало, откуда будем двигаться дальше, это же номер вершины-начала нового резистора, который будет суммой всех последующих
        int start=curr;
        //Конечный индекс, который будем соединять с начальным, образуя тем самым новый резистор
        int finish=curr;

        //Пока текущая вершина не является окончанием последовательного участка цепи, т.е. если из нее исходит более одного ребра(начало параллельного соединения)
        //или если в нее направлено более одного ребра(точка окончания параллельного соединения)
       while (checkForEndOfSerial(adjMatrix, curr)){
            //Индекс вершины, куда мы хотим попасть, т.е. следующей вершины в последовательном соединении
           int whereWeWantToMove =0;
           //Ищем его
           for (int i=0;i<adjMatrix[curr].length;i++){
               if (adjMatrix[curr][i]){
                   whereWeWantToMove=i;
                   break;
               }
           }
           //Прибавляем к значению сопротивления значение между текущей вершиной и вершиной, куда мы хотим попасть
           R+=resistors[curr][whereWeWantToMove];
           //Удаляем старый резистор
           adjMatrix[curr][whereWeWantToMove]=false;
           //И двигаемся дальше
           curr=whereWeWantToMove;
           finish=whereWeWantToMove;

       }
        //Стягиваем стартовую и конечную вершины, таким образом создавая новый резистор
       adjMatrix[start][finish]=true;
       resistors[start][finish]=R;
       //ТАким образом заменяем несколько последовательных ребер на одно ребро
       return R;

    }

    /**
     * Поиск индекса, на котором окончится последовательное соединение соединение
     * @param adjMatrix - матрица
     * @param curr - текущий индекс
     * @return
     */
    public static int searchForEndOfSerial(boolean[][] adjMatrix,int curr){
        //Индекс следующей вершины, куда стремимся
        int next=curr;
        //Счетчики входящих и выходящих в/из верш. next
        int countOfInput =0;
        int countOfOutput =0;
        //Ищем индекс последующей ячейки
        for (int i=0;i<adjMatrix[curr].length;i++){
            if (adjMatrix[curr][i]){
                next=i;
                break;
            }
        }

        //Ищем число ребер, входящих или выходящих из
        for (int i=0;i<adjMatrix[next].length;i++){

            if (adjMatrix[i][next]) {
                System.out.println("  "+i+" "+next+"  ");
                countOfInput++;
            }
            if (adjMatrix[next][i]) countOfOutput++;
        }
        //Если один из этих счетчиков больше единицы, то вершина next - это последняя вершина в последовательном соединении, возвращаем ее
        if (countOfInput>1 || countOfOutput>1){
            return next;
        }
        //Если же в вершину next можно попасть только через curr и next, в свою очередь, ведет только в одну вершину,
        // то можем двигаться дальше и рекурсивно вызываем алгоритм для вершины next
        else if(countOfInput==1 || countOfOutput==1) return searchForEndOfSerial(adjMatrix,next);
        //Если же счетчик остался равен нулю, то это конец графа, и текущая вершина - конец соединения
        else return curr;
    }


    /**
     * Провека на то, является ли вершина curr последней в последовательном соединении
     * @param adjMatrix - матрица
     * @param curr - текущая вершина
     * @return
     */
    public static boolean checkForEndOfSerial(boolean[][] adjMatrix,int curr){
//        int next=0;
        int countOfInput =0;
        int countOfOutput=0;
        //Считаем кол-во входящих и исходящих ребер для вершины curr
        for (int i=0;i<adjMatrix[curr].length;i++){
            if (adjMatrix[i][curr]) countOfInput++;
            if (adjMatrix[curr][i]) countOfOutput++;
        }
        //Если любое из них больше единицы, это значит, что данный узел - последний перед начинающимся параллельным соединением(или наоборот, кончающимся)
        //Случай countOfOutput==0 означает окончание графа
        if ((countOfInput>1 || countOfOutput>1)||countOfOutput==0){
            return true;
        }
        return false;
    }

    public static double itsFinalBitch(boolean[][] adjMatrix,double[][] resistors,int curr){

        //Пока цепь не является простой
        while (!checkForSimplicity(adjMatrix)){
            //Вспомогашки для вызова searchingForTheMostNestedParallelConnection
            int k=0;
            boolean[] visited = new boolean[adjMatrix.length];
            for (int i=0;i< visited.length;i++){
                visited[i]=false;
            }
            System.out.println(k);
            //Ищем индекс вершины, с которой начнется самое вложенное параллельное соединение
            //индекс запишется в k
            searchingForTheMostNestedParallelConnection(adjMatrix,curr,0,visited);
            //И упрощаем это соединение
            simplifyParallel(adjMatrix,resistors,k);
        }
        //В конечном счете, когда все параллельности будут устранены, упрощаем получившееся последовательное соединение
        return simplifySerial(adjMatrix,resistors,curr,0);
    }


        //Метод по расчету сопротивления цепи
    public static double resistance(boolean[][] adjMatrix, double[][] resistors, int curr, double R, boolean[] visited){

        //Первое -- выясняем, последовательное ли соединение, или параллельное
        // Для этого заводим счетчик смежных вершин
        // Если есть путь между вершинами curr и i,
        // а так же мы еще не ходили в i(по идее, для орграфа мы не можем вернуться назад, но на всякий случай добавил и эту проверку)
        // то счетчик наращивается
        int count =0;
        for (int i=0;i<adjMatrix[curr].length;i++){
            if(adjMatrix[curr][i] && !visited[i]) count++;
        }

        //Если значение счетчика больше единицы, т.е. есть более двух смежных вершин, рассматриваем вариант параллельного соединения
        if (count>1){

            //Заводим переменнные под числитель и знаменатель
            double Chislitel = 1;
            double Znamenatel = 1;
            //Проходимся по всем вершинам, смежным с текущей
            for (int i=0;i< adjMatrix[curr].length;i++){
            //Если путь есть, и мы еще не посещали вершину i, то

                if (adjMatrix[curr][i] && !visited[i]){
                    //Посещаем ее
                    visited[curr]=true;
                    //Проверки-вспомогашки
                    System.out.println(" curRes:"+resistors[curr][i]+"  cur"+(curr+1)+" to"+(i+1)+"  R"+R);
                    //Домножаем числитель на сопротивление текущего резистора, а так же рекурсивно вызываем функцию для i-й вершины
                    //Аналогично со знаменателем, но там уже складываем
                    Chislitel*=resistors[curr][i]*resistance(adjMatrix, resistors, i, R,visited);
                    Znamenatel+=resistors[curr][i]+resistance(adjMatrix, resistors, i, R,visited);

                }

            }
            //В итоге прибавляем к конечному сопротивлению цепи полученную дробь, как если бы это было последовательное соединение
            R+=Chislitel/Znamenatel;
            System.out.println("Chi"+Chislitel+" Zn"+Znamenatel);

        }
        //Случай последовательного соединения
        //Проходимся по всем смежным вершинам(вообще-то она будет одна, и ее можно было бы найти заранее, но...)
        else for (int i=0;i<adjMatrix[curr].length;i++){
            //Если есть путь в i-ю вершину, и мы ее еще не посетили
            if (adjMatrix[curr][i] && !visited[i]){
                //Посещаем
                visited[curr]=true;
                System.out.println(" curRes:"+resistors[curr][i]+"  cur"+(curr+1)+" to"+(i+1)+"  R"+R);
                //И попросту приплюсовываем к конечному сопротивлению сопр-е текущей ячейки + рекусивно вызываем для i-й вершины
                R+=resistors[curr][i]+resistance(adjMatrix, resistors, i, R, visited);
            }
        }

        return R;
    }


    //Проверяем на работоспособность
    public static void testOfNormalWork(){

        //Проверка на пропускание тока через граф
        boolean[][] graph = {
                {false,true,true,true,false,false,false,false,false},
                {true,false,false,false,true,false,false,false,false},
                {true,false,false,false,false,false,true,false,false},
                {true,false,false,false,false,true,false,false,false},
                {false,true,false,false,false,true,false,true,false},
                {false,false,false,true,true,false,false,false,false},
                {false,false,true,false,false,true,false,false,true},
                {false,false,false,false,true,false,false,false,false},
                {false,false,false,false,false,false,true,false,false}
        };

        graph= GraphAlgorithms.bfsMy(graph,0,5);
        for (boolean[] bArr : graph){
            System.out.println();
            for (boolean b : bArr){
                System.out.print(" "+b);
            }
        }


        //Проверка на поиск сопртивления
        boolean[][] newGraph = {
                {false,true,true,true,false,false,false},
                {false,false,false,false,true,false,false},
                {false,false,false,false,false,false,true},
                {false,false,false,false,false,true,false},
                {false,false,false,false,false,true,false},
                {false,false,false,false,false,false,false},
                {false,false,false,false,false,true,false}
        };
        double[][] resistors = {
                {0,5,6,9,0,0,0},
                {0,0,0,0,6,0,0},
                {0,0,0,0,0,0,6},
                {0,0,0,0,0,21,0},
                {0,0,0,0,0,8,0},
                {0,0,0,0,0,0,0},
                {0,0,0,0,0,1,0}
        };

        boolean[] visited = {false,false,false,false,false,false,false};


//        boolean[][] bbrbrbrb =  {
//            {false,true,false,false,false,false,false,false,false,false,false,false},
//            {false,false,true,false,false,false,false,false,false,false,false,false},
//            {false,false,false,true,true,true,false,false,false,false,false,false},
//            {false,false,false,false,false,false,false,true,false,false,false,false},
//            {false,false,false,false,false,false,false,false,false,false,false,true},
//            {false,false,false,false,false,false,true,false,false,false,false,false},
//            {false,false,false,false,false,false,false,true,false,false,false,false},
//            {false,false,false,false,false,false,false,false,false,false,false,true},
//            {false,false,false,false,false,false,false,false,false,false,false,false},
//            {false,false,false,false,false,false,false,false,false,false,false,false},
//            {false,false,false,false,false,false,false,false,false,false,false,false},
//            {false,false,false,false,false,false,false,false,false,false,false,false}
//        };
//
//        boolean[][] newGraph = {
//                {false,true,false,false,false},
//                {false,false, true,false,false},
//                {false,false, false,true,true},
//                {false,false, false,false,false},
//                {false,false, false,false,false}
//
//        };
//        double[][] resistors={
//                {0,5,0,0,0},
//                {0,0,6,0,0},
//                {0,0,0,4,5},
//                {0,0,0,0,0},
//                {0,0,0,0,0}
//        };
      //          boolean[] visited ={false,false,false,false,false};

        System.out.println();
        System.out.println(GraphAlgorithms.resistance(newGraph,resistors,0,0,visited));
        System.out.println();
        System.out.println();
        System.out.println(itsFinalBitch(newGraph,resistors,0));
    }

    public static List<List<Integer>> allPathSourceTarget(Graph graph,int start,int finish){

        boolean[] visited = new boolean[graph.vertexCount()];
        List<List<Integer>> result = new ArrayList<>();
        List<Integer> path = new ArrayList<>();
        dfs(graph,result,path,start,finish, visited);
        return result;

    }

    public static Set<Integer> commonVertices(List<List<Integer>> list){

        Set<Integer> set = new HashSet<>();
        set.addAll(list.get(0));

        for (List<Integer> integerList : list){

            Set<Integer> result = new HashSet<>();
            result.addAll(integerList);

           set.retainAll(result);

        }

        return set;
    }


    /**
     * Поиск в глубину, реализованный с помощью стека
     * (не совсем "правильный"/классический, т.к. "в глубину" реализуется только "план" обхода, а не сам обход)
     * @param graph граф
     * @param from Вершина, с которой начинается поиск
     * @param visitor Посетитель
     */
    public static void dfs(Graph graph, int from, Consumer<Integer> visitor) {
        boolean[] visited = new boolean[graph.vertexCount()];
        Stack<Integer> stack = new Stack<Integer>();
        stack.push(from);
        visited[from] = true;
        while (!stack.empty()) {
            Integer curr = stack.pop();
            visitor.accept(curr);
            for (Integer v : graph.adjacencies(curr)) {
                if (!visited[v]) {
                    stack.push(v);
                    visited[v] = true;
                }
            }
        }
    }

    /**
     * Поиск в ширину, реализованный с помощью очереди
     * (начальная вершина также включена)
     * @param graph граф
     * @param from Вершина, с которой начинается поиск
     * @param visitor Посетитель
     */
    public static void bfs(Graph graph, int from, Consumer<Integer> visitor) {
        boolean[] visited = new boolean[graph.vertexCount()];
        Queue<Integer> queue = new LinkedList<Integer>();
        queue.add(from);
        visited[from] = true;
        while (queue.size() > 0) {
            Integer curr = queue.remove();
            visitor.accept(curr);
            for (Integer v : graph.adjacencies(curr)) {
                if (!visited[v]) {
                    queue.add(v);
                    visited[v] = true;
                }
            }
        }
    }


    /**
     * Поиск в глубину в виде итератора
     * (начальная вершина также включена)
     * @param graph граф
     * @param from Вершина, с которой начинается поиск
     * @return Итератор
     */
    public static Iterable<Integer> dfs(Graph graph, int from) {
        return new Iterable<Integer>() {
            private Stack<Integer> stack = null;
            private boolean[] visited = null;

            @Override
            public Iterator<Integer> iterator() {
                stack = new Stack<>();
                stack.push(from);
                visited = new boolean[graph.vertexCount()];
                visited[from] = true;

                return new Iterator<Integer>() {
                    @Override
                    public boolean hasNext() {
                        return ! stack.isEmpty();
                    }

                    @Override
                    public Integer next() {
                        Integer result = stack.pop();
                        for (Integer adj : graph.adjacencies(result)) {
                            if (!visited[adj]) {
                                visited[adj] = true;
                                stack.add(adj);
                            }
                        }
                        return result;
                    }
                };
            }
        };
    }

    /**
     * Поиск в ширину в виде итератора
     * (начальная вершина также включена)
     * @param from Вершина, с которой начинается поиск
     * @return Итератор
     */
    public static Iterable<Integer> bfs(Graph graph, int from) {
        return new Iterable<Integer>() {
            private Queue<Integer> queue = null;
            private boolean[] visited = null;

            @Override
            public Iterator<Integer> iterator() {
                queue = new LinkedList<>();
                queue.add(from);
                visited = new boolean[graph.vertexCount()];
                visited[from] = true;

                return new Iterator<Integer>() {
                    @Override
                    public boolean hasNext() {
                        return ! queue.isEmpty();
                    }

                    @Override
                    public Integer next() {
                        Integer result = queue.remove();
                        for (Integer adj : graph.adjacencies(result)) {
                            if (!visited[adj]) {
                                visited[adj] = true;
                                queue.add(adj);
                            }
                        }
                        return result;
                    }
                };
            }
        };
    }
}
