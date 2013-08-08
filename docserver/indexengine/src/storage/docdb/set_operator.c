/*
 * =====================================================================================
 *
 *       Filename:  hi_set_operator.c
 *
 *    Description:  ��rowid���ϵĽ���������������㣬����hash��������
 *
 *        Version:  1.0
 *        Created:  2011/05/06 
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  yuzhong.zhao
 *        Company:  alipay
 *
 * =====================================================================================
 */

#include "set_operator.h"

void print_seg_rowid_list(struct list_head* list) {
	if (list == NULL ) {
		return;
	}

	struct segment_query_rowids* qresult;

	list_for_each_entry(qresult, list, rowids_list) {
		printf("seg id: %d\n", qresult->sid);
		print_rowid_list(qresult->rowids);
	}
}

/**
 *	������segment���Ӧ��rowid���ϵĲ�����ע�⼯�������ı������е�����
 *	@param		pMemPool	�ڴ�أ���Ŀǰ��ʵ���в���ͨ���ڴ�����������ڴ棬���Ժ���Ż������п��ܻ������������ڴ� 
 *	@param 		lista
 *	@param 		listb
 *	@return		
 */
struct list_head* seg_rowid_union(MEM_POOL_PTR pMemPool,
		struct list_head* lista, struct list_head* listb) {
	if (lista == NULL || listb == NULL ) {
		log_error("error when processing rowid set union, the input is null!");
		return NULL ;
	}

	struct segment_query_rowids* qresult;
	struct segment_query_rowids* qresulta;
	struct segment_query_rowids* qresultb;

	struct list_head* rlist = (struct list_head*) mem_pool_malloc(pMemPool,
			sizeof(struct list_head));
	INIT_LIST_HEAD(rlist);

	double_list_for_each_entry(qresulta, lista, qresultb, listb, rowids_list) {
		if (qresulta->sid != qresultb->sid) {
			log_error(
					"error when processing rowid set union, the segid %d and %d are different!", qresulta->sid, qresultb->sid);
			return NULL ;
		}
		qresult = (struct segment_query_rowids*) mem_pool_malloc(pMemPool,
				sizeof(struct segment_query_rowids));
		memset(qresult, 0, sizeof(struct segment_query_rowids));
		qresult->sid = qresulta->sid;
		qresult->rowids = rowid_union(pMemPool, qresulta->rowids,
				qresultb->rowids);
		list_add_tail(&qresult->rowids_list, rlist);
	}

	return rlist;
}

/**
 *	������segment���Ӧ��rowid���ϵĽ�����ע�⼯�������ı������е�����
 *	@param		pMemPool	�ڴ�أ���Ŀǰ��ʵ���в���ͨ���ڴ�����������ڴ棬���Ժ���Ż������п��ܻ������������ڴ� 
 *	@param 		lista
 *	@param 		listb
 *	@return		
 */
struct list_head* seg_rowid_intersection(MEM_POOL_PTR pMemPool,
		struct list_head* lista, struct list_head* listb) {
	if (lista == NULL || listb == NULL ) {
		log_error(
				"error when processing rowid set intersection, the input is null!");
		return NULL ;
	}

	struct segment_query_rowids* qresult;
	struct segment_query_rowids* qresulta;
	struct segment_query_rowids* qresultb;
	struct list_head* rlist = (struct list_head*) mem_pool_malloc(pMemPool,
			sizeof(struct list_head));
	INIT_LIST_HEAD(rlist);

	double_list_for_each_entry(qresulta, lista, qresultb, listb, rowids_list) {
		if (qresulta->sid != qresultb->sid) {
			log_error(
					"error when processing rowid set intersection, the segid %d and %d are different!", qresulta->sid, qresultb->sid);
			return NULL ;
		}
		qresult = (struct segment_query_rowids*) mem_pool_malloc(pMemPool,
				sizeof(struct segment_query_rowids));
		memset(qresult, 0, sizeof(struct segment_query_rowids));
		qresult->sid = qresulta->sid;
		qresult->rowids = rowid_intersection(pMemPool, qresulta->rowids,
				qresultb->rowids);
		list_add_tail(&qresult->rowids_list, rlist);
	}

	return rlist;
}

/**
 *	������segment���Ӧ��rowid���ϵĲ��ע�⼯�������ı������е�����
 *	@param		pMemPool	�ڴ�أ���Ŀǰ��ʵ���в���ͨ���ڴ�����������ڴ棬���Ժ���Ż������п��ܻ������������ڴ� 
 *	@param 		lista
 *	@param 		listb
 *	@return		
 */
struct list_head* seg_rowid_minus(MEM_POOL_PTR pMemPool,
		struct list_head* lista, struct list_head* listb) {
	if (lista == NULL || listb == NULL ) {
		log_error("error when processing rowid set minus, the input is null!");
		return NULL ;
	}

	struct segment_query_rowids* qresult;
	struct segment_query_rowids* qresulta;
	struct segment_query_rowids* qresultb;
	struct list_head* rlist = (struct list_head*) mem_pool_malloc(pMemPool,
			sizeof(struct list_head));
	INIT_LIST_HEAD(rlist);

	double_list_for_each_entry(qresulta, lista, qresultb, listb, rowids_list) {
		if (qresulta->sid != qresultb->sid) {
			log_error(
					"error when processing rowid set minus, the segid %d and %d are different!", qresulta->sid, qresultb->sid);
			return NULL ;
		}
		qresult = (struct segment_query_rowids*) mem_pool_malloc(pMemPool,
				sizeof(struct segment_query_rowids));
		memset(qresult, 0, sizeof(struct segment_query_rowids));
		qresult->sid = qresulta->sid;
		qresult->rowids = rowid_minus(pMemPool, qresulta->rowids,
				qresultb->rowids);
		list_add_tail(&qresult->rowids_list, rlist);
	}

	return rlist;
}

struct list_head* seg_rowid_fulltext_hence(MEM_POOL_PTR pMemPool,
		struct list_head* lista, struct list_head* listb) {
	if (lista == NULL || listb == NULL ) {
		log_error(
				"error when processing rowid set fulltext hence, the input is null!");
		return NULL ;
	}

	struct segment_query_rowids* qresult;
	struct segment_query_rowids* qresulta;
	struct segment_query_rowids* qresultb;
	struct list_head* rlist = (struct list_head*) mem_pool_malloc(pMemPool,
			sizeof(struct list_head));
	INIT_LIST_HEAD(rlist);

	double_list_for_each_entry(qresulta, lista, qresultb, listb, rowids_list) {
		if (qresulta->sid != qresultb->sid) {
			log_error(
					"error when processing rowid set fulltext hence, the segid %d and %d are different!", qresulta->sid, qresultb->sid);
			return NULL ;
		}
		qresult = (struct segment_query_rowids*) mem_pool_malloc(pMemPool,
				sizeof(struct segment_query_rowids));
		memset(qresult, 0, sizeof(struct segment_query_rowids));
		qresult->sid = qresulta->sid;
		qresult->rowids = rowid_fulltext_hence(pMemPool, qresulta->rowids,
				qresultb->rowids);
		list_add_tail(&qresult->rowids_list, rlist);
	}

	return rlist;
}

int32_t seg_rowid_count(struct list_head* seglist) {
	int32_t count = 0;

	if (seglist == NULL ) {
		return 0;
	}

	struct segment_query_rowids* qresult;
	list_for_each_entry(qresult, seglist, rowids_list) {
		if (qresult->rowids != NULL ) {
			count += qresult->rowids->rowid_num;
		}
	}

	return count;
}

void seg_rowid_setscore(MEM_POOL_PTR mem_pool, struct list_head* seglist,
		double score) {
	if (seglist == NULL ) {
		return;
	}

	struct segment_query_rowids* qresult;

	list_for_each_entry(qresult, seglist, rowids_list) {
		rowid_list_setscore(mem_pool, qresult->rowids, score);
	}
}

/**
 *	������rowid���ϵĲ�����ע�⼯�������ı������е����ݣ����������lista�У�rowid������������ģ�����ʽΪ����
 *	@param		pMemPool	�ڴ�أ���Ŀǰ��ʵ���в���ͨ���ڴ�����������ڴ棬���Ժ���Ż������п��ܻ������������ڴ� 
 *	@param 		lista
 *	@param 		listb
 *	@return		
 */
struct rowid_list* rowid_union(MEM_POOL_PTR pMemPool, struct rowid_list* lista,
		struct rowid_list* listb) {
	//��һ��rowid����Ϊ��
	if (lista == NULL || lista->rowid_num == 0) {
		return listb;
	}
	//�ڶ���rowid����Ϊ��
	if (listb == NULL || listb->rowid_num == 0) {
		return lista;
	}

	//�������
	struct rowid_list* result = rowid_list_init(pMemPool);

	//p��ǵ�ǰ���ڴ����lista�е������
	struct rowid_list_node* p = lista->head;
	//q��ǵ�ǰ���ڴ����listb�е������	
	struct rowid_list_node* q = listb->head;
	//r��ǲ��������ǰ�����
	struct rowid_list_node* r = (struct rowid_list_node*) mem_pool_malloc(
			pMemPool, sizeof(struct rowid_list_node));
	memset(r, 0, sizeof(struct rowid_list_node));
	result->head = r;

	//i��ǵ�ǰ���ڴ����lista�е�Ԫ�ر��
	//j��ǵ�ǰ���ڴ����listb�е�Ԫ�ر��
	//k��ǵ�ǰ���ڴ���Ľ�������е�Ԫ�ر��
	uint32_t i = 0, j = 0, k = 0;

	while (i < lista->rowid_num && j < listb->rowid_num) {
		if (p->rowid_array[i % ROWID_ARRAY_SIZE]
				> q->rowid_array[j % ROWID_ARRAY_SIZE]) {
			r->rowid_array[k % ROWID_ARRAY_SIZE] = p->rowid_array[i
					% ROWID_ARRAY_SIZE];
			if (p->score_array != NULL ) {
				if (r->score_array == NULL ) {
					r->score_array = (double*) mem_pool_malloc(pMemPool,
							sizeof(double) * ROWID_ARRAY_SIZE);
					memset(r->score_array, 0,
							sizeof(double) * ROWID_ARRAY_SIZE);
				}
				r->score_array[k % ROWID_ARRAY_SIZE] = p->score_array[i
						% ROWID_ARRAY_SIZE];
			}

			i++;
			k++;

			if (i % ROWID_ARRAY_SIZE == 0) {
				p = p->next;
			}

		} else if (p->rowid_array[i % ROWID_ARRAY_SIZE]
				< q->rowid_array[j % ROWID_ARRAY_SIZE]) {
			r->rowid_array[k % ROWID_ARRAY_SIZE] = q->rowid_array[j
					% ROWID_ARRAY_SIZE];
			if (q->score_array != NULL ) {
				if (r->score_array == NULL ) {
					r->score_array = (double*) mem_pool_malloc(pMemPool,
							sizeof(double) * ROWID_ARRAY_SIZE);
					memset(r->score_array, 0,
							sizeof(double) * ROWID_ARRAY_SIZE);
				}
				r->score_array[k % ROWID_ARRAY_SIZE] = q->score_array[j
						% ROWID_ARRAY_SIZE];
			}

			j++;
			k++;
			if (j % ROWID_ARRAY_SIZE == 0) {
				q = q->next;
			}
		} else {
			r->rowid_array[k % ROWID_ARRAY_SIZE] = p->rowid_array[i
					% ROWID_ARRAY_SIZE];
			if (p->score_array != NULL || q->score_array != NULL ) {
				if (r->score_array == NULL ) {
					r->score_array = (double*) mem_pool_malloc(pMemPool,
							sizeof(double) * ROWID_ARRAY_SIZE);
					memset(r->score_array, 0,
							sizeof(double) * ROWID_ARRAY_SIZE);
				}

				if (p->score_array != NULL ) {
					r->score_array[k % ROWID_ARRAY_SIZE] += p->score_array[i
							% ROWID_ARRAY_SIZE];
				}
				if (q->score_array != NULL ) {
					r->score_array[k % ROWID_ARRAY_SIZE] += q->score_array[j
							% ROWID_ARRAY_SIZE];
				}
			}

			i++;
			j++;
			k++;

			if (i % ROWID_ARRAY_SIZE == 0) {
				p = p->next;
			}
			if (j % ROWID_ARRAY_SIZE == 0) {
				q = q->next;
			}
		}

		if (k % ROWID_ARRAY_SIZE == 0) {
			r->next = (struct rowid_list_node*) mem_pool_malloc(pMemPool,
					sizeof(struct rowid_list_node));
			memset(r->next, 0, sizeof(struct rowid_list_node));
			r = r->next;
		}

	}

	//�ڶ��������Ѿ�������
	if (j == listb->rowid_num) {
		j = i;
		q = p;
		listb = lista;
	}

	//��ʣ���Ԫ�����μ��뵽���������
	while (j < listb->rowid_num) {
		r->rowid_array[k % ROWID_ARRAY_SIZE] = q->rowid_array[j
				% ROWID_ARRAY_SIZE];
		if (q->score_array != NULL ) {
			if (r->score_array == NULL ) {
				r->score_array = (double*) mem_pool_malloc(pMemPool,
						sizeof(double) * ROWID_ARRAY_SIZE);
				memset(r->score_array, 0, sizeof(double) * ROWID_ARRAY_SIZE);
			}
			r->score_array[k % ROWID_ARRAY_SIZE] = q->score_array[j
					% ROWID_ARRAY_SIZE];
		}

		j++;
		k++;

		if (j % ROWID_ARRAY_SIZE == 0) {
			q = q->next;
		}

		if (k % ROWID_ARRAY_SIZE == 0) {
			r->next = (struct rowid_list_node*) mem_pool_malloc(pMemPool,
					sizeof(struct rowid_list_node));
			memset(r->next, 0, sizeof(struct rowid_list_node));
			r = r->next;
		}

	}

	result->rowid_num = k;
	result->tail = r;
	return result;

}

/**
 *	������rowid���ϵĽ�����ע�⼯�������ı������е����ݣ����������lista�У�rowid������������ģ�����ʽΪ����
 *	@param		pMemPool	�ڴ�أ���Ŀǰ��ʵ���в���ͨ���ڴ�����������ڴ棬���Ժ���Ż������п��ܻ������������ڴ� 
 *	@param 		lista
 *	@param 		listb
 *	@return		
 */
struct rowid_list* rowid_intersection(MEM_POOL_PTR pMemPool,
		struct rowid_list* lista, struct rowid_list* listb) {
	//��һ��rowid����Ϊ��
	if (lista == NULL || lista->rowid_num == 0) {
		return lista;
	}
	//�ڶ���rowid����Ϊ��	
	if (listb == NULL || listb->rowid_num == 0) {
		return listb;
	}

	//�������
	struct rowid_list* result = rowid_list_init(pMemPool);

	//p��ǵ�ǰ���ڴ����lista�е������
	struct rowid_list_node* p = lista->head;
	//q��ǵ�ǰ���ڴ����listb�е������	
	struct rowid_list_node* q = listb->head;
	//r��ǲ��������ǰ�����
	struct rowid_list_node* r = (struct rowid_list_node*) mem_pool_malloc(
			pMemPool, sizeof(struct rowid_list_node));
	memset(r, 0, sizeof(struct rowid_list_node));
	result->head = r;

	//i��ǵ�ǰ���ڴ����lista�е�Ԫ�ر��
	//j��ǵ�ǰ���ڴ����listb�е�Ԫ�ر��
	//k��ǵ�ǰ���ڴ���Ľ�������е�Ԫ�ر��
	uint32_t i = 0, j = 0, k = 0;

	while (i < lista->rowid_num && j < listb->rowid_num) {
		if (p->rowid_array[i % ROWID_ARRAY_SIZE]
				> q->rowid_array[j % ROWID_ARRAY_SIZE]) {
			i++;

			if (i % ROWID_ARRAY_SIZE == 0) {
				p = p->next;
			}
		} else if (p->rowid_array[i % ROWID_ARRAY_SIZE]
				< q->rowid_array[j % ROWID_ARRAY_SIZE]) {
			j++;

			if (j % ROWID_ARRAY_SIZE == 0) {
				q = q->next;
			}
		} else {
			r->rowid_array[k % ROWID_ARRAY_SIZE] = p->rowid_array[i
					% ROWID_ARRAY_SIZE];
			if (p->score_array != NULL || q->score_array != NULL ) {
				if (r->score_array == NULL ) {
					r->score_array = (double*) mem_pool_malloc(pMemPool,
							sizeof(double) * ROWID_ARRAY_SIZE);
					memset(r->score_array, 0,
							sizeof(double) * ROWID_ARRAY_SIZE);
				}

				if (p->score_array != NULL ) {
					r->score_array[k % ROWID_ARRAY_SIZE] += p->score_array[i
							% ROWID_ARRAY_SIZE];
				}
				if (q->score_array != NULL ) {
					r->score_array[k % ROWID_ARRAY_SIZE] += q->score_array[j
							% ROWID_ARRAY_SIZE];
				}
			}

			i++;
			j++;
			k++;

			if (i % ROWID_ARRAY_SIZE == 0) {
				p = p->next;
			}
			if (j % ROWID_ARRAY_SIZE == 0) {
				q = q->next;
			}
			if (k % ROWID_ARRAY_SIZE == 0) {
				r->next = (struct rowid_list_node*) mem_pool_malloc(pMemPool,
						sizeof(struct rowid_list_node));
				memset(r->next, 0, sizeof(struct rowid_list_node));
				r = r->next;
			}
		}
	}

	if (k == 0) {
		lista->rowid_num = 0;
		lista->head = NULL;
		lista->tail = NULL;
		return lista;
	}

	result->rowid_num = k;
	result->tail = r;
	return result;
}

struct rowid_list* rowid_fulltext_hence(MEM_POOL_PTR pMemPool,
		struct rowid_list* lista, struct rowid_list* listb) {
	//��һ��rowid����Ϊ��
	if (lista == NULL || lista->rowid_num == 0) {
		return lista;
	}
	//�ڶ���rowid����Ϊ��
	if (listb == NULL || listb->rowid_num == 0) {
		return lista;
	}

	//���
	struct rowid_list* result = lista;

	//p��ǵ�ǰ���ڴ����lista�е������
	struct rowid_list_node* p = lista->head;
	//q��ǵ�ǰ���ڴ����listb�е������
	struct rowid_list_node* q = listb->head;
	//r��ǲ��������ǰ�����
	struct rowid_list_node* r = p;

	//i��ǵ�ǰ���ڴ����lista�е�Ԫ�ر��
	//j��ǵ�ǰ���ڴ����listb�е�Ԫ�ر��
	//k��ǵ�ǰ���ڴ���Ľ�������е�Ԫ�ر��
	uint32_t i = 0, j = 0, k = 0;

	while (i < lista->rowid_num && j < listb->rowid_num) {
		if (p->rowid_array[i % ROWID_ARRAY_SIZE]
				> q->rowid_array[j % ROWID_ARRAY_SIZE]) {

			r->rowid_array[k % ROWID_ARRAY_SIZE] = p->rowid_array[i
					% ROWID_ARRAY_SIZE];
			r->score_array[k % ROWID_ARRAY_SIZE] = p->score_array[i
					% ROWID_ARRAY_SIZE];
			i++;
			if (i % ROWID_ARRAY_SIZE == 0) {
				p = p->next;
			}
			k++;
			if (k % ROWID_ARRAY_SIZE == 0) {
				r = r->next;
			}
		} else if (p->rowid_array[i % ROWID_ARRAY_SIZE]
				< q->rowid_array[j % ROWID_ARRAY_SIZE]) {
			j++;

			if (j % ROWID_ARRAY_SIZE == 0) {
				q = q->next;
			}
		} else {
			r->rowid_array[k % ROWID_ARRAY_SIZE] = p->rowid_array[i
					% ROWID_ARRAY_SIZE];
			r->score_array[k % ROWID_ARRAY_SIZE] = p->score_array[i
					% ROWID_ARRAY_SIZE] + q->score_array[j % ROWID_ARRAY_SIZE];

			i++;
			j++;
			k++;

			if (i % ROWID_ARRAY_SIZE == 0) {
				p = p->next;
			}
			if (j % ROWID_ARRAY_SIZE == 0) {
				q = q->next;
			}
			if (k % ROWID_ARRAY_SIZE == 0) {
				r = r->next;
			}
		}
	}

	if (k == 0) {
		lista->rowid_num = 0;
		lista->head = NULL;
		lista->tail = NULL;
		return lista;
	}

	result->rowid_num = k;
	result->tail = r;
	return result;
}




/**
 *	������rowid���ϵĲ��ע�⼯�������ı������е����ݣ����������lista�У�rowid������������ģ�����ʽΪ����
 *	@param		pMemPool	�ڴ�أ���Ŀǰ��ʵ���в���ͨ���ڴ�����������ڴ棬���Ժ���Ż������п��ܻ������������ڴ� 
 *	@param 		lista
 *	@param 		listb
 *	@return		
 */
struct rowid_list* rowid_minus(MEM_POOL_PTR pMemPool, struct rowid_list* lista,
		struct rowid_list* listb) {
	//������������һ������Ϊ��
	if (lista == NULL || listb == NULL || lista->rowid_num == 0
			|| listb->rowid_num == 0) {
		return lista;
	}

	//p��ǵ�ǰ���ڴ����lista�е������
	struct rowid_list_node* p = lista->head;
	//q��ǵ�ǰ���ڴ����listb�е������	
	struct rowid_list_node* q = listb->head;
	//result��ǵ�ǰ���ڴ���Ĳ����е������
	struct rowid_list_node* result = lista->head;
	//pre_result���result��ǰһ�������
	struct rowid_list_node* pre_result = NULL;

	//i��ǵ�ǰ���ڴ����lista�е�Ԫ�ر��
	//j��ǵ�ǰ���ڴ����listb�е�Ԫ�ر��
	//k��ǵ�ǰ���ڴ���Ľ�������е�Ԫ�ر��
	uint32_t i = 0, j = 0, k = 0;

	while (i < lista->rowid_num && j < listb->rowid_num) {
		if (p->rowid_array[i % ROWID_ARRAY_SIZE]
				> q->rowid_array[j % ROWID_ARRAY_SIZE]) {
			result->rowid_array[k % ROWID_ARRAY_SIZE] = p->rowid_array[i
					% ROWID_ARRAY_SIZE];
			i++;
			k++;

			if (i % ROWID_ARRAY_SIZE == 0) {
				p = p->next;
			}
			if (k % ROWID_ARRAY_SIZE == 0) {
				pre_result = result;
				result = result->next;
			}

		} else if (p->rowid_array[i % ROWID_ARRAY_SIZE]
				< q->rowid_array[j % ROWID_ARRAY_SIZE]) {
			j++;

			if (j % ROWID_ARRAY_SIZE == 0) {
				q = q->next;
			}
		} else {
			i++;
			j++;

			if (i % ROWID_ARRAY_SIZE == 0) {
				p = p->next;
			}
			if (j % ROWID_ARRAY_SIZE == 0) {
				q = q->next;
			}
		}

	}

	//�����һ�����ϻ���ʣ���Ԫ�أ����������μ��뵽���������
	while (i < lista->rowid_num) {
		result->rowid_array[k % ROWID_ARRAY_SIZE] = p->rowid_array[i
				% ROWID_ARRAY_SIZE];
		i++;
		k++;

		if (i % ROWID_ARRAY_SIZE == 0) {
			p = p->next;
		}
		if (k % ROWID_ARRAY_SIZE == 0) {
			pre_result = result;
			result = result->next;
		}
	}

	if (k == 0) {
		lista->rowid_num = 0;
		lista->head = NULL;
		lista->tail = NULL;
		return lista;
	}

	if (k % ROWID_ARRAY_SIZE == 0) {
		result = pre_result;
	}

	result->next = NULL;
	lista->rowid_num = k;
	lista->tail = result;

	return lista;

}

void rowid_list_setscore(MEM_POOL_PTR mem_pool, struct rowid_list* id_list,
		double score) {
	uint32_t i;
	//ָ��rowid�������ָ��
	struct rowid_list_node* p;

	if (id_list == NULL ) {
		return;
	}
	for (p = id_list->head, i = 0; i < id_list->rowid_num;) {
		if (NULL == p->score_array) {
			p->score_array = (double*) mem_pool_malloc(mem_pool,
					sizeof(double) * ROWID_ARRAY_SIZE);
			memset(p->score_array, 0, sizeof(double) * ROWID_ARRAY_SIZE);
		}
		p->score_array[i % ROWID_ARRAY_SIZE] = score;

		i++;
		if (i % ROWID_ARRAY_SIZE == 0) {
			p = p->next;
		}
	}
}
