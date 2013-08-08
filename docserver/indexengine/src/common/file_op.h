#include "def.h"
#include <dirent.h>
#include <sys/stat.h>
#include <unistd.h>
#include <sys/types.h>

#ifndef FILE_OP_H
#define FILE_OP_H


#ifndef S_IRWXUGO
# define S_IRWXUGO (S_IRWXU | S_IRWXG | S_IRWXO)
#endif


/**
  * �ж�block���ļ���С�����С��size����0
  * @param  split_block block��Ϣ
  * @param  size ��Ҫ��֤�Ĵ�С
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 
 int32_t ensure_file_size(int32_t fd,int32_t size);




 /**
   * �����ļ�����һ���ļ��������ļ���fd
   * @param  file_name ��Ҫ�򿪵��ļ���
   * @return �ɹ�����fd��ʧ�ܷ���-1
   **/ 
 int32_t open_file(const char *file_name,int32_t flag);



 /**
   * �����ļ���ƫ����
   * @param  fd �ļ�������
   * @return �ɹ������ļ�ƫ����
   **/ 
 off_t get_position(int32_t fd);


 /**
   * �����ļ��Ĵ�С
   * @param  fd �ļ�������
   * @return �ɹ������ļ���С
   **/ 
 uint64_t get_file_size(uint32_t fd);


  /**
   * �ر��ļ�
   * @param  fd �ļ�������
   * @return �ɹ�����0��ʧ�ܷ���-1
   **/ 
 int32_t close_file(int32_t fd);


 /**
  * ����Ŀ¼
  * @param	dir_path Ŀ¼��
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 
 int32_t mkdirs(char *dir_path);



 /**
  * ����Ŀ¼
  * @param	filename ԭ�е��ļ�
  * @param  new_filename ���ļ�������
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 
 int32_t cp_dir(const char* filename,const char* new_filename);



 /**
  * �ж�dir_pathĿ¼�Ƿ����
  * @param	dir_path Ŀ¼��
  * @return ��Ŀ¼�Ļ�����1�����ǻ򲻴����򷵻�0
  **/ 
 int32_t is_dir(const char *dir_path);


 /**
  * �ƶ��ļ�filename���µ�Ŀ¼��
  * @param	filename �ϵ��ļ�·��
  * @param  newdir  �ƶ����µ�Ŀ¼
  * @param  nid     �ڵ��
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 
 int32_t mv_file(const char* filename,const char* newdir,uint16_t nid);

 /**
  * ����һ��linkָ��filename
  * @param	filename �ϵ��ļ�·��
  * @param  newdir  �ƶ����µ�Ŀ¼
  * @param  nid     �ڵ��
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 
 int32_t link_file(const char* filename,const char* newdir,uint16_t nid);


 /**
  * ����fdΪ������ģʽ
  * @param	fd �ļ�������
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 
 int32_t set_file_noblock(int32_t fd);


 /**
  * �ж�dir_path�Ƿ�ΪĿ¼
  * @param	dir_path 
  * @return �����Ŀ¼������1�����򷵻�0
  **/
 int32_t is_directory(const char *dir_path);

 /**
  * ��ȡ�����ڴ�
  * @param	�ļ���
  * @param ��Ҫӳ����ڴ��С
  * @return �ɹ������ڴ棬ʧ��ΪNULL
  **/
 void* get_mmap_memory(char* filename,uint32_t size);

/**
 * Alloc memory from os, initialized by file content. Free alloced memory by munmap.
 * Create file, if file not exist.
 * @param filename
 * @param size memory size.
 * @return alloced memory on success, NULL on error.
 */
void *alloc_file_memory(const char *filename, uint32_t size);

/**
 * Save memory to file, flushed to disk.
 * @param filename
 * @param mem
 * @param size, memory size
 * @param write_limit write throughout limit, byte per second.
 * @return on success 0 is returned, on error -1 is returned.
 */
int flush_memory(const char *filename, void *mem, uint32_t size, uint32_t write_limit);

/**
 * Switch mmaped file.
 * @param filename file switch to.
 * @param mem mem must be a multiple of the page size.
 * @param size
 * @return on success 0 is returned, on error -1 is returned.
 */
int switch_mmaped_file(const char *filename, void *mem, uint32_t size);


//����ص�����
typedef int32_t (*traversal_dir_callback)(char* dir,void* arg);

/**
 * ������һ����Ŀ¼
 * @param  dir ��Ŀ¼��
 * @param  func �ص�����
 * @param  arg �ص������Ĳ���
 * @return �ɹ�����0��ʧ��<0
 **/
int32_t traversal_single_deep_childdir(char* dir,traversal_dir_callback func,void* arg);

// uninterruptable read, same with read in unistd.h
// on error, errno is set appropriately
ssize_t unintr_read(int fd, void *buf, size_t count);

// uninterruptable write, same with write in unistd.h
// on error, errno is set appropriately
ssize_t unintr_write(int fd, const void *buf, size_t count);

// uninterruptable pread, same with pread in unistd.h
// on error, errno is set appropriately
ssize_t unintr_pread(int fd, void *buf, size_t count, off_t offset);

// uninterruptable pwrite, same with pwrite in unistd.h
// on error, errno is set appropriately
ssize_t unintr_pwrite(int fd, const void *buf, size_t count, off_t offset);

#endif // FILE_OP_H
