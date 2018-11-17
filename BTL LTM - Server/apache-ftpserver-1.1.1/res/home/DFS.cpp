#include <iostream> 
#include <stack>
using namespace std;

int a[100][100], vs[10]={0}, n=7;

void input_matrix(int n, int a[100][100]){
	int i, j;
	for (i=1;i<=n;i++)
		for (j=1;j<=n;j++){
			cin>>a[i][j];
		}
}


void DFS(int u){		//set up without using libary
	int v;
	cout<<u<<" ";
	vs[u]=1;
	for (v=1;v<=n;v++)
		if (vs[v]==0&&a[u][v]==1)
			DFS(v);
}



int main(){
	input_matrix(n,a);
	DFS(2);
}
