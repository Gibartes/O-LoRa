#pragma once

/* Doubly Linked List */
#ifndef offsetof
#define offsetof(TYPE,MEMBER) ((unsigned long) &((TYPE *)0)->MEMBER)
#endif
#define container_of(ptr,type,member) ({    \
    const typeof( ((type *)0)->member) *__mptr = (ptr);	\
    (type *)((char *)__mptr - offsetof(type,member)); })

struct list_head{
    struct list_head *next,*prev;};

#define list_head_init(name) { &(name), &(name) }
#define list_head(name) \
    struct list_head name = list_head_init(name)

/* get the struct for this entry */
#define list_entry(ptr,type,member) \
    container_of(ptr,type,member)
/* get the first element from a test */
#define list_first_entry(ptr,type,member) \
    list_entry((ptr)->next,type,member)
/* iterate over a list */
#define list_cursor_update(cursor,head) \
    for(cursor=(head)->next;cursor!=(head);cursor=cursor->next)

/* initialize list head */
static inline void init_list_head(struct list_head *list){
    list->next = list;
    list->prev = list;
}

/* Insert a new entry after the specified head. */
static inline void __list_add(struct list_head *new,
								struct list_head *prev,
								struct list_head *next){
    next->prev 	= new;
    new->next 	= next;
    new->prev	= prev;
    prev->next	= new;
}
/* Insert a new entry after the specified head. ( Stack ) */
static inline void list_add(struct list_head *new, struct list_head *head){
    __list_add(new,head,head->next);
}
/* Insert a new entry after the specified head. ( Queue ) */
static inline void list_add_tail(struct list_head *new, struct list_head *head){
    __list_add(new,head->prev,head);
}
/* Delete a list entry by making the prev/next entries. */
static inline void __list_del(struct list_head *prev,
								struct list_head *next){
    next->prev = prev;
    prev->next = next;
}
/* Replace old entry by new one */
static inline void list_replace(struct list_head *old,struct list_head *new){
    new->next = old->next;
    new->next->prev = new;
    new->prev = old->prev;
    new->prev->next = new;
}
/* Replace entry from list and reinitialize it */
static inline void list_replace_init(struct list_head *old,struct list_head *new){
    list_replace(old,new);
    init_list_head(old);
}
/* Delete entry from list */
static inline void list_del(struct list_head *entry){
    __list_del(entry->prev,entry->next);
}
/* Delete entry from list and reinitialize it */
static inline void list_del_init(struct list_head *entry){
    __list_del(entry->prev,entry->next);
    init_list_head(entry);
}
/* Delete from one list and add as another's head */
static inline void list_move(struct list_head *list,struct list_head *head){
    __list_del(list->prev,list->next);
    list_add(list,head);
}
/* Delete from one list and add as another's tail */
static inline void list_move_tail(struct list_head *list,struct list_head *head){
    __list_del(list->prev,list->next);
    list_add_tail(list,head);
}
/* Tests whether list is the last entry in list head */
static inline int list_is_last(const struct list_head *list,const struct list_head *head){
    return list->next == head;
}
/* Tests whether list is empty */
static inline int list_empty(const struct list_head *head){
    return head->next == head;
}
