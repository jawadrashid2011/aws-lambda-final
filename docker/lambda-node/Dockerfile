FROM public.ecr.aws/lambda/nodejs:20

COPY getSessions.mjs ${LAMBDA_TASK_ROOT}
COPY terminateSessions.mjs ${LAMBDA_TASK_ROOT}

# COPY test.mjs ${LAMBDA_TASK_ROOT}

COPY ./node_modules ${LAMBDA_TASK_ROOT}/node_modules

CMD [ "getSessions.handler" ]
# CMD [ "terminateSessions.handler" ]